package pl.masslany.podkop.features.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.DropdownMenu
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.favorites.preview.FavoritesScreenStateProvider
import pl.masslany.podkop.features.favorites.preview.NoOpFavoritesActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.favorites_empty_entries
import podkop.composeapp.generated.resources.favorites_empty_entry_comments
import podkop.composeapp.generated.resources.favorites_empty_everything
import podkop.composeapp.generated.resources.favorites_empty_link_comments
import podkop.composeapp.generated.resources.favorites_empty_links
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.topbar_label_favorites

private const val FabItemsOffset = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<FavoritesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = "${state.screenInstanceId}:${state.sortMenuState.selected}:${state.typeMenuState.selected}",
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    FavoritesScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    paddingValues: PaddingValues,
    state: FavoritesScreenState,
    actions: FavoritesActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isScrollingUp = lazyListState.isScrollingUp()
    val showFab by remember(isScrollingUp) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > FabItemsOffset && isScrollingUp
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_favorites))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
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
                windowInsets = topBarInsets,
            )
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
                        FavoritesScreenList(
                            state = state,
                            actions = actions,
                            lazyListState = lazyListState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesScreenList(
    state: FavoritesScreenState,
    actions: FavoritesActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = 16.dp,
        ),
    ) {
        item(key = "DropdownMenuRow") {
            Row {
                DropdownMenu(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            start = 16.dp,
                        ),
                    items = state.sortMenuState.items,
                    selected = state.sortMenuState.selected,
                    expanded = state.sortMenuState.expanded,
                    onSelected = actions::onSortSelected,
                    onExpandedChange = actions::onSortExpandedChanged,
                    onDismissRequest = actions::onSortDismissed,
                )
                DropdownMenu(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            start = 16.dp,
                        ),
                    items = state.typeMenuState.items,
                    selected = state.typeMenuState.selected,
                    expanded = state.typeMenuState.expanded,
                    onSelected = actions::onTypeSelected,
                    onExpandedChange = actions::onTypeExpandedChanged,
                    onDismissRequest = actions::onTypeDismissed,
                )
            }
        }

        items(
            items = state.resources,
            key = { item -> item.id },
            contentType = { item -> item.contentType },
        ) {
            ResourceItemRenderer(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = it,
                actions = actions,
                config = ResourceItemConfig(
                    showReplyAction = true,
                    showLinkCommentReplyAction = false,
                    renderCommentAsCard = true,
                ),
            )
        }

        if (state.resources.isEmpty()) {
            item(key = "EmptyState") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.typeMenuState.selected.toEmptyMessageLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.isPaginating) {
            item(key = "PaginationLoadingIndicator") {
                PaginationLoadingIndicator()
            }
        }
    }
}

@Composable
private fun DropdownMenuItemType.toEmptyMessageLabel(): String = when (this) {
    DropdownMenuItemType.Everything -> stringResource(Res.string.favorites_empty_everything)
    DropdownMenuItemType.Links -> stringResource(Res.string.favorites_empty_links)
    DropdownMenuItemType.Entries -> stringResource(Res.string.favorites_empty_entries)
    DropdownMenuItemType.LinkComments -> stringResource(Res.string.favorites_empty_link_comments)
    DropdownMenuItemType.EntryComments -> stringResource(Res.string.favorites_empty_entry_comments)
    else -> ""
}

@Preview
@Composable
private fun FavoritesScreenContentPreview(
    @PreviewParameter(FavoritesScreenStateProvider::class) state: FavoritesScreenState,
) {
    PodkopPreview(darkTheme = false) {
        FavoritesScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpFavoritesActions,
            lazyListState = rememberLazyListState(),
        )
    }
}
