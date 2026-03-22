package pl.masslany.podkop.features.rank

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.rememberWindowSizeClass
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.rank.components.RankRow
import pl.masslany.podkop.features.rank.components.RankTableHeader
import pl.masslany.podkop.features.rank.preview.RankPreviewActions
import pl.masslany.podkop.features.rank.preview.RankScreenStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.topbar_label_rank

private val RankContentMaxWidth = 1080.dp
val RankPositionColumnWidth = 60.dp
val RankCompactActionsColumnWidth = 96.dp
val RankMetricColumnWidth = 112.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RankScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<RankViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    RankScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RankScreenContent(
    paddingValues: PaddingValues,
    state: RankScreenState,
    actions: RankActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = rememberWindowSizeClass()
    val showExtendedColumns = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false, includeBottom = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_rank))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = topBarInsets,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues),
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = actions::onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }

                    state.isError -> {
                        GenericErrorScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                            onRefreshClicked = actions::onRefresh,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 12.dp,
                                end = 16.dp,
                                bottom = WindowInsets
                                    .navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding() + 16.dp,
                            ),
                        ) {
                            rankStickyHeader(showExtendedColumns = showExtendedColumns)

                            items(
                                items = state.items,
                                key = { item -> item.username },
                            ) { item ->
                                RankRowContainer {
                                    RankRow(
                                        item = item,
                                        showExtendedColumns = showExtendedColumns,
                                        onClick = {
                                            actions.onUserClicked(item.username)
                                        },
                                    )
                                }
                            }

                            if (state.isPaginating) {
                                item(key = "rank-pagination-loading") {
                                    RankRowContainer {
                                        PaginationLoadingIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.rankStickyHeader(
    showExtendedColumns: Boolean,
) {
    stickyHeader(key = "rank-header") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            RankTableHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = RankContentMaxWidth)
                    .padding(bottom = 8.dp),
                showExtendedColumns = showExtendedColumns,
            )
        }
    }
}

@Composable
fun RankRowContainer(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = RankContentMaxWidth),
        ) {
            content()
        }
    }
}

@Preview(name = "Rank Compact", widthDp = 390, heightDp = 844)
@Composable
private fun RankScreenContentCompactPreview(
    @PreviewParameter(RankScreenStateProvider::class) state: RankScreenState,
) {
    PodkopPreview(darkTheme = false) {
        RankScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = RankPreviewActions,
            lazyListState = LazyListState(),
        )
    }
}

@Preview(name = "Rank Wide", widthDp = 960, heightDp = 720)
@Composable
private fun RankScreenContentWidePreview(
    @PreviewParameter(RankScreenStateProvider::class) state: RankScreenState,
) {
    PodkopPreview(darkTheme = false) {
        RankScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = RankPreviewActions,
            lazyListState = LazyListState(),
        )
    }
}
