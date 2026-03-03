package pl.masslany.podkop.features.entries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.DropdownMenu
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.composer.Composer
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.nestedScrollConnection
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.entries.preview.EntriesScreenStateProvider
import pl.masslany.podkop.features.entries.preview.NoOpEntriesActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_add
import podkop.composeapp.generated.resources.accessibility_topbar_profile
import podkop.composeapp.generated.resources.entries_composer_hint
import podkop.composeapp.generated.resources.ic_add
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_person
import podkop.composeapp.generated.resources.topbar_label_entries

private const val FAB_ITEMS_OFFSET = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntriesScreenRoot(
    paddingValues: PaddingValues,
    onEntryClicked: ((Int) -> Unit)? = null,
    onEntryReplyClicked: ((entryId: Int, author: String?) -> Unit)? = null,
    onEntryCommentReplyClicked: ((entryId: Int, entryCommentId: Int, author: String?) -> Unit)? = null,
) {
    val viewModel = koinViewModel<EntriesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onEntryClicked) {
        viewModel.entryCreatedNavigation.collect { entryId ->
            val navigationOverride = onEntryClicked
            if (navigationOverride != null) {
                navigationOverride(entryId)
            } else {
                viewModel.onEntryClicked(entryId)
            }
        }
    }
    val actions = remember(
        viewModel,
        onEntryClicked,
        onEntryReplyClicked,
        onEntryCommentReplyClicked,
    ) {
        object : EntriesActions by viewModel {
            override fun onEntryClicked(id: Int) {
                val navigationOverride = onEntryClicked
                if (navigationOverride != null) {
                    navigationOverride(id)
                } else {
                    viewModel.onEntryClicked(id)
                }
            }

            override fun onEntryReplyClicked(entryId: Int, author: String?) {
                val navigationOverride = onEntryReplyClicked
                if (navigationOverride != null) {
                    navigationOverride(entryId, author)
                } else {
                    viewModel.onEntryReplyClicked(entryId, author)
                }
            }

            override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
                val navigationOverride = onEntryCommentReplyClicked
                if (navigationOverride != null) {
                    navigationOverride(entryId, entryCommentId, author)
                } else {
                    viewModel.onEntryCommentReplyClicked(entryId, entryCommentId, author)
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
    EntriesScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = actions,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesScreenContent(
    paddingValues: PaddingValues,
    state: EntriesScreenState,
    actions: EntriesActions,
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
    val composerBottomPadding = if (state.composer.isVisible) {
        if (state.composer.photoUrl != null) 304.dp else 232.dp
    } else {
        0.dp
    }

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
                title = { Text(text = stringResource(resource = Res.string.topbar_label_entries)) },
                actions = {
                    IconButton(onClick = actions::onTopBarAddEntryClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_add),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_add,
                            ),
                        )
                    }
                    IconButton(onClick = actions::onTopBarProfileClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_person),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_profile,
                            ),
                        )
                    }
                },
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
                    EntriesScreenList(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = state,
                        actions = actions,
                        lazyListState = lazyListState,
                        composerBottomPadding = composerBottomPadding,
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
            visible = showFab && !state.composer.isVisible,
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

        Composer(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = state.composer,
            hintText = stringResource(resource = Res.string.entries_composer_hint),
            onContentChanged = actions::onComposerTextChanged,
            onAdultChanged = actions::onComposerAdultChanged,
            onPhotoAttachClicked = actions::onComposerPhotoAttachClicked,
            onPhotoRemoved = actions::onComposerPhotoRemoved,
            onDismiss = actions::onComposerDismissed,
            onSubmit = actions::onComposerSubmit,
        )

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
private fun EntriesScreenList(
    modifier: Modifier = Modifier,
    state: EntriesScreenState,
    actions: EntriesActions,
    lazyListState: LazyListState,
    composerBottomPadding: Dp,
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
            bottom = systemBottomPadding + composerBottomPadding,
        ),
    ) {
        item(
            key = "DropdownMenuRow",
        ) {
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
                state.hotSortMenuState?.let {
                    DropdownMenu(
                        modifier = Modifier
                            .padding(
                                top = 8.dp,
                                start = 16.dp,
                            ),
                        items = state.hotSortMenuState.items,
                        selected = state.hotSortMenuState.selected,
                        expanded = state.hotSortMenuState.expanded,
                        onSelected = actions::onHotSortSelected,
                        onExpandedChange = actions::onHotSortExpandedChanged,
                        onDismissRequest = actions::onHotSortDismissed,
                    )
                }
            }
        }

        items(
            items = state.entries,
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

@Preview
@Composable
private fun EntriesScreenContentPreview(
    @PreviewParameter(EntriesScreenStateProvider::class) state: EntriesScreenState,
) {
    PodkopPreview(darkTheme = false) {
        EntriesScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpEntriesActions,
            lazyListState = rememberLazyListState(),
        )
    }
}
