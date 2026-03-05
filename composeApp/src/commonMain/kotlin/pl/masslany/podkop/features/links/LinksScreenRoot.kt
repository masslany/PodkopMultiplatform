package pl.masslany.podkop.features.links

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.coerceIn
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
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.nestedScrollConnection
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.links.hits.HitsList
import pl.masslany.podkop.features.links.preview.LinksScreenStateProvider
import pl.masslany.podkop.features.links.preview.NoOpLinksActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.topbar_label_homepage
import podkop.composeapp.generated.resources.topbar_label_upcoming

private const val FAB_ITEMS_OFFSET = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinksScreenRoot(
    isUpcoming: Boolean,
    paddingValues: PaddingValues,
    onLinkClicked: ((Int) -> Unit)? = null,
) {
    val viewModel = koinViewModel<LinksViewModel>(
        parameters = { parametersOf(isUpcoming) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onLinkClicked) {
        object : LinksActions by viewModel {
            override fun onLinkClicked(id: Int) {
                if (onLinkClicked != null) {
                    onLinkClicked(id)
                } else {
                    viewModel.onLinkClicked(id)
                }
            }
        }
    }

    val lazyListState = rememberLazyListPaginator(
        resetStateKey = state.screenInstanceId,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    LinksScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = actions,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinksScreenContent(
    paddingValues: PaddingValues,
    state: LinksScreenState,
    actions: LinksActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bottomBarScrollBehavior = LocalBottomBarScrollBehavior.current
    val isScrollingUp = lazyListState.isScrollingUp()
    val showFab by remember(isScrollingUp) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > FAB_ITEMS_OFFSET && isScrollingUp
        }
    }
    val density = LocalDensity.current
    val bottomPadding by remember {
        derivedStateOf {
            with(density) {
                val offset = bottomBarScrollBehavior.offsetPx.toDp()
                val height = bottomBarScrollBehavior.heightPx.toDp()
                (height - offset).coerceIn(0.dp, height)
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            )
            .fillMaxSize()
            .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopAppBar(
                title = { Text(text = getTopBarTitle(state.isUpcoming)) },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { actions.onRefresh(state.sortMenuState.selected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                        onRefreshClicked = { actions.onRefresh(state.sortMenuState.selected) },
                    )
                } else {
                    LinksScreenList(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = state,
                        actions = actions,
                        lazyListState = lazyListState,
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = 16.dp + bottomPadding,
                ),
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(bottomPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinksScreenList(
    modifier: Modifier = Modifier,
    state: LinksScreenState,
    actions: LinksActions,
    lazyListState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = WindowInsets
                .navigationBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp,
        ),
    ) {
        if (!state.isUpcoming) {
            item {
                HitsList(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = state.hits,
                    actions = actions,
                )
            }
        }

        item {
            DropdownMenu(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                items = state.sortMenuState.items,
                selected = state.sortMenuState.selected,
                expanded = state.sortMenuState.expanded,
                onSelected = actions::onSortSelected,
                onExpandedChange = actions::onSortExpandedChanged,
                onDismissRequest = actions::onSortDismissed,
            )
        }

        items(
            items = state.links,
            key = { item -> item.id },
            contentType = { item -> item.contentType },
        ) {
            ResourceItemRenderer(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                state = it,
                actions = actions,
                config = ResourceItemConfig(
                    showEntryInlineActions = false,
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

@Composable
private fun getTopBarTitle(isUpcoming: Boolean): String = if (isUpcoming) {
    stringResource(resource = Res.string.topbar_label_upcoming)
} else {
    stringResource(resource = Res.string.topbar_label_homepage)
}

@Preview
@Composable
private fun LinksScreenContentPreview(
    @PreviewParameter(LinksScreenStateProvider::class) state: LinksScreenState,
) {
    PodkopPreview(darkTheme = false) {
        LinksScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpLinksActions,
            lazyListState = rememberLazyListState(),
        )
    }
}
