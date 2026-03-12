package pl.masslany.podkop.features.tag

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import pl.masslany.podkop.common.pagination.rememberLazyStaggeredGridPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import pl.masslany.podkop.features.tag.components.TagBanner
import pl.masslany.podkop.features.tag.components.TagContentError
import pl.masslany.podkop.features.tag.components.TagDetails
import pl.masslany.podkop.features.tag.components.TagGalleryImageItem
import pl.masslany.podkop.features.tag.preview.NoOpTagActions
import pl.masslany.podkop.features.tag.preview.TagScreenStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_gallery_view
import podkop.composeapp.generated.resources.accessibility_topbar_list_view
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_view_list
import podkop.composeapp.generated.resources.ic_view_module
import podkop.composeapp.generated.resources.tag_details_screen_gallery_no_images

private const val FabItemsOffset = 10
private val TagGalleryItemMinSize = 160.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun TagScreenRoot(
    tag: String,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<TagViewModel>(parameters = { parametersOf(tag) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = state.screenInstanceId,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            !state.isGalleryMode && viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    val lazyStaggeredGridState = rememberLazyStaggeredGridPaginator(
        resetStateKey = state.screenInstanceId,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            state.isGalleryMode && viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
        lastVisibleIndexProvider = { gridState ->
            state.toGalleryPaginationIndex(gridState)
        },
        totalItemsCountProvider = {
            state.resources.size
        },
    )
    TagScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
        lazyStaggeredGridState = lazyStaggeredGridState,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun TagScreenContent(
    paddingValues: PaddingValues,
    state: TagScreenState,
    actions: TagActions,
    lazyListState: LazyListState,
    lazyStaggeredGridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isScrollingUp = if (state.isGalleryMode) {
        lazyStaggeredGridState.isScrollingUp()
    } else {
        lazyListState.isScrollingUp()
    }
    val showFab by remember(state.isGalleryMode, isScrollingUp) {
        derivedStateOf {
            val firstVisibleItemIndex = if (state.isGalleryMode) {
                lazyStaggeredGridState.firstVisibleItemIndex
            } else {
                lazyListState.firstVisibleItemIndex
            }
            firstVisibleItemIndex > FabItemsOffset && isScrollingUp
        }
    }
    val showTitle by remember(state.isGalleryMode) {
        derivedStateOf {
            val firstVisibleItemIndex = if (state.isGalleryMode) {
                lazyStaggeredGridState.firstVisibleItemIndex
            } else {
                lazyListState.firstVisibleItemIndex
            }
            val firstVisibleItemScrollOffset = if (state.isGalleryMode) {
                lazyStaggeredGridState.firstVisibleItemScrollOffset
            } else {
                lazyListState.firstVisibleItemScrollOffset
            }
            firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var pendingToggleAnchorResourceIndex by remember(state.screenInstanceId) {
        mutableStateOf<Int?>(null)
    }

    LaunchedEffect(
        state.isGalleryMode,
        state.resources,
        state.galleryItems,
        pendingToggleAnchorResourceIndex,
    ) {
        val anchorResourceIndex = pendingToggleAnchorResourceIndex ?: return@LaunchedEffect
        if (state.isGalleryMode) {
            val galleryIndex = state.findClosestGalleryIndex(anchorResourceIndex)
            val targetIndex = if (galleryIndex != null) {
                state.headerItemsCount + galleryIndex
            } else {
                state.headerItemsCount
            }
            lazyStaggeredGridState.scrollToItem(index = targetIndex)
        } else if (state.resources.isNotEmpty()) {
            val targetResourceIndex = anchorResourceIndex.coerceIn(0, state.resources.lastIndex)
            lazyListState.scrollToItem(index = state.headerItemsCount + targetResourceIndex)
        }
        pendingToggleAnchorResourceIndex = null
    }

    Scaffold(
        modifier = modifier
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
                    IconButton(
                        onClick = {
                            pendingToggleAnchorResourceIndex = if (state.isGalleryMode) {
                                state.findVisibleGalleryResourceIndex(lazyStaggeredGridState)
                            } else {
                                state.findVisibleListResourceIndex(lazyListState)
                            }
                            actions.onGalleryModeToggled()
                        },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(
                                resource = if (state.isGalleryMode) {
                                    Res.drawable.ic_view_list
                                } else {
                                    Res.drawable.ic_view_module
                                },
                            ),
                            contentDescription = stringResource(
                                resource = if (state.isGalleryMode) {
                                    Res.string.accessibility_topbar_list_view
                                } else {
                                    Res.string.accessibility_topbar_gallery_view
                                },
                            ),
                        )
                    }
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
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
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
                            if (state.isGalleryMode) {
                                lazyStaggeredGridState.animateScrollToItem(0)
                            } else {
                                lazyListState.animateScrollToItem(0)
                            }
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
            onRefresh = actions::onRefresh,
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
                    onRefreshClicked = actions::onRefresh,
                )
            } else {
                if (state.isGalleryMode) {
                    TagScreenGallery(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        actions = actions,
                        lazyStaggeredGridState = lazyStaggeredGridState,
                    )
                } else {
                    TagScreenList(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        actions = actions,
                        lazyListState = lazyListState,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagScreenList(
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
        screenHeader(state = state, actions = actions)

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
                config = ResourceItemConfig(
                    showReplyAction = true,
                    isReplyActionEnabled = state.isLoggedIn,
                ),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TagScreenGallery(
    modifier: Modifier = Modifier,
    state: TagScreenState,
    actions: TagActions,
    lazyStaggeredGridState: LazyStaggeredGridState,
) {
    val systemBottomPadding = WindowInsets
        .systemBars
        .asPaddingValues()
        .calculateBottomPadding()

    LazyVerticalStaggeredGrid(
        modifier = modifier,
        state = lazyStaggeredGridState,
        columns = StaggeredGridCells.Adaptive(TagGalleryItemMinSize),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = systemBottomPadding + 16.dp,
        ),
    ) {
        screenHeader(state = state, actions = actions)

        if (state.galleryItems.isEmpty()) {
            item(
                key = "TagGalleryEmpty",
                span = StaggeredGridItemSpan.FullLine,
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp),
                    text = stringResource(resource = Res.string.tag_details_screen_gallery_no_images),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(
                items = state.galleryItems,
                key = { item -> "gallery_${item.entryId}_${item.imageUrl}" },
            ) { item ->
                TagGalleryImageItem(
                    item = item,
                    actions = actions,
                )
            }
        }

        if (state.isPaginating) {
            item(
                key = "PaginationLoadingIndicator",
                span = StaggeredGridItemSpan.FullLine,
            ) {
                PaginationLoadingIndicator()
            }
        }
    }
}

private fun LazyListScope.screenHeader(
    state: TagScreenState,
    actions: TagActions,
) {
    item(
        key = "TagBanner",
    ) {
        TagBanner(bannerUrl = state.bannerUrl)
    }

    item(
        key = "TagDetails",
    ) {
        TagDetails(
            state = state,
            actions = actions,
        )
    }

    if (state.isTagContentError) {
        item(
            key = "TagContentError",
        ) {
            TagContentError()
        }
    }

    item(
        key = "TagFilters",
    ) {
        TagFilters(
            state = state,
            actions = actions,
        )
    }
}

private fun LazyStaggeredGridScope.screenHeader(
    state: TagScreenState,
    actions: TagActions,
) {
    item(
        key = "TagBanner",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        TagBanner(bannerUrl = state.bannerUrl)
    }

    item(
        key = "TagName",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        TagDetails(
            state = state,
            actions = actions,
        )
    }

    if (state.isTagContentError) {
        item(
            key = "TagContentError",
            span = StaggeredGridItemSpan.FullLine,
        ) {
            TagContentError()
        }
    }

    item(
        key = "TagFilters",
        span = StaggeredGridItemSpan.FullLine,
    ) {
        TagFilters(
            state = state,
            actions = actions,
        )
    }
}

@Composable
private fun TagFilters(
    state: TagScreenState,
    actions: TagActions,
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

private fun TagScreenState.toGalleryPaginationIndex(
    gridState: LazyStaggeredGridState,
): Int? {
    val lastVisibleGridIndex = gridState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: return null
    if (resources.isEmpty()) {
        return null
    }

    if (galleryItems.isEmpty()) {
        return resources.lastIndex
    }

    val lastVisibleGalleryIndex = lastVisibleGridIndex - headerItemsCount
    return when {
        lastVisibleGalleryIndex >= galleryItems.lastIndex -> resources.lastIndex
        lastVisibleGalleryIndex < 0 -> null
        else -> galleryItems.getOrNull(lastVisibleGalleryIndex)?.resourceIndex
    }
}

private fun TagScreenState.findVisibleListResourceIndex(
    listState: LazyListState,
): Int? {
    val firstVisibleListContentIndex = listState.layoutInfo.visibleItemsInfo
        .map { it.index }
        .filter { it >= headerItemsCount }
        .minOrNull()
        ?: return null

    val resourceIndex = firstVisibleListContentIndex - headerItemsCount
    return resourceIndex.takeIf { it in resources.indices }
}

private fun TagScreenState.findVisibleGalleryResourceIndex(
    gridState: LazyStaggeredGridState,
): Int? {
    val firstVisibleGalleryContentIndex = gridState.layoutInfo.visibleItemsInfo
        .map { it.index }
        .filter { it >= headerItemsCount }
        .minOrNull()
        ?: return null

    val galleryIndex = firstVisibleGalleryContentIndex - headerItemsCount
    return galleryItems.getOrNull(galleryIndex)?.resourceIndex
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun TagScreenContentPreview(
    @PreviewParameter(TagScreenStateProvider::class) state: TagScreenState,
) {
    PodkopPreview(darkTheme = false) {
        TagScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpTagActions,
            lazyListState = rememberLazyListState(),
            lazyStaggeredGridState = rememberLazyStaggeredGridState(),
        )
    }
}
