package pl.masslany.podkop.features.tag

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.DropdownMenu
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_profile
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_person
import podkop.composeapp.generated.resources.tag_details_screen_error_loading_tags

private const val FabItemsOffset = 10
private val TagBannerHeight = 160.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagScreenRoot(
    tag: String,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<TagViewModel>(parameters = { parametersOf(tag) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = LocalAppSnackbarHostState.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = state.screenInstanceId,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    val isScrollingUp = lazyListState.isScrollingUp()
    val showFab by remember(isScrollingUp) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > FabItemsOffset && isScrollingUp
        }
    }
    val showTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            )
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (showTitle) {
                        Text(text = "#${state.tag}")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onTopBarProfileClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_person),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_profile,
                            ),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_back,
                            ),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollBehavior.state.heightOffset = 0f
                            scrollBehavior.state.contentOffset = 0f
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = vectorResource(resource = Res.drawable.ic_keyboard_arrow_up),
                        contentDescription = stringResource(
                            resource = Res.string.accessibility_fab_scroll_to_top,
                        ),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPaddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPaddingValues.calculateTopPadding()),
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            } else if (state.isError) {
                GenericErrorScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    onRefreshClicked = viewModel::onRefresh,
                )
            } else {
                TagScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    actions = viewModel,
                    lazyListState = lazyListState,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagScreen(
    modifier: Modifier = Modifier,
    state: TagScreenState,
    actions: TagActions,
    lazyListState: LazyListState,
) {
    val systemBottomPadding = WindowInsets
        .systemBars
        .asPaddingValues()
        .calculateBottomPadding()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = systemBottomPadding + 16.dp,
        ),
    ) {
        item(
            key = "TagBanner",
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TagBannerHeight),
                model = Builder(LocalPlatformContext.current)
                    .data(state.bannerUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            )
        }

        item(
            key = "TagName",
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "#${state.tag}",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        if (state.isTagContentError) {
            item(
                key = "TagContentError",
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(resource = Res.string.tag_details_screen_error_loading_tags),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        item(
            key = "TagFilters",
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DropdownMenu(
                    items = state.typeMenuState.items,
                    selected = state.typeMenuState.selected,
                    expanded = state.typeMenuState.expanded,
                    onSelected = actions::onTypeSelected,
                    onExpandedChange = actions::onTypeExpandedChanged,
                    onDismissRequest = actions::onTypeDismissed,
                )

                DropdownMenu(
                    items = state.sortMenuState.items,
                    selected = state.sortMenuState.selected,
                    expanded = state.sortMenuState.expanded,
                    onSelected = actions::onSortSelected,
                    onExpandedChange = actions::onSortExpandedChanged,
                    onDismissRequest = actions::onSortDismissed,
                )
            }
        }

        items(
            items = state.resources,
            key = { item -> item.id },
            contentType = { item -> item.contentType },
        ) {
            ResourceItemRenderer(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                state = it,
                actions = actions,
            )
        }

        if (state.isPaginating) {
            item(
                key = "PaginationLoadingIndicator",
            ) {
                PaginationLoadingIndicator()
            }
        }
    }
}
