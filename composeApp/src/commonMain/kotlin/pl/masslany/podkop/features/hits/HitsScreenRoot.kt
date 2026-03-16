package pl.masslany.podkop.features.hits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
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
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.hits.archivepicker.HitsArchivePickerDialog
import pl.masslany.podkop.features.hits.archivepicker.HitsArchivePickerState
import pl.masslany.podkop.features.hits.archivepicker.HitsArchiveState
import pl.masslany.podkop.features.hits.preview.NoOpHitsScreenActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.topbar_label_hits

private const val FabItemsOffset = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HitsScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<HitsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = buildString {
            append(state.sortMenuState.selected)
            append(':')
            append(state.selectedArchive?.year)
            append(':')
            append(state.selectedArchive?.month)
        },
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    HitsScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HitsScreenContent(
    paddingValues: PaddingValues,
    state: HitsScreenState,
    actions: HitsActions,
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
    val contentInsets = paddingValues.toWindowInsets(includeTop = false, includeBottom = false)
    val layoutDirection = LocalLayoutDirection.current
    val bottomInsetPadding = paddingValues.calculateBottomPadding()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_hits))
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
        contentWindowInsets = contentInsets,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPaddingValues ->
        val bodyPadding = PaddingValues(
            start = innerPaddingValues.calculateStartPadding(layoutDirection),
            top = innerPaddingValues.calculateTopPadding(),
            end = innerPaddingValues.calculateEndPadding(layoutDirection),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bodyPadding),
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
                        HitsScreenList(
                            state = state,
                            actions = actions,
                            lazyListState = lazyListState,
                            bottomContentPadding = bottomInsetPadding + 16.dp,
                        )
                    }
                }
            }
        }
    }

    state.archivePickerState?.let { archivePickerState ->
        HitsArchivePickerDialog(
            state = archivePickerState,
            onDismissRequest = actions::onArchiveDismissed,
            onPreviousYearClick = actions::onArchivePreviousYearClicked,
            onNextYearClick = actions::onArchiveNextYearClicked,
            onMonthClick = actions::onArchiveMonthClicked,
            onConfirm = actions::onArchiveConfirmed,
        )
    }
}

@Composable
private fun HitsScreenList(
    state: HitsScreenState,
    actions: HitsActions,
    lazyListState: LazyListState,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = bottomContentPadding,
        ),
    ) {
        item(key = "filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DropdownMenu(
                    items = state.sortMenuState.items,
                    selected = state.sortMenuState.selected,
                    expanded = state.sortMenuState.expanded,
                    onSelected = actions::onSortSelected,
                    onExpandedChange = actions::onSortExpandedChanged,
                    onDismissRequest = actions::onSortDismissed,
                )

                FilterChip(
                    selected = state.selectedArchive != null,
                    onClick = actions::onArchiveClicked,
                    label = {
                        Text(
                            text = state.selectedArchive?.label
                                ?: stringResource(resource = HitsArchivePickerState.archiveButtonLabelRes),
                        )
                    },
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
                    showEntryInlineActions = false,
                ),
            )
        }

        if (state.isPaginating) {
            item(key = "PaginationLoadingIndicator") {
                PaginationLoadingIndicator()
            }
        }
    }
}

@Preview
@Composable
private fun HitsScreenContentPreview() {
    PodkopPreview(darkTheme = false) {
        HitsScreenContent(
            paddingValues = PaddingValues(),
            state = HitsScreenState.initial.copy(
                isLoading = false,
                sortMenuState = DropdownMenuState(
                    items = persistentListOf(
                        DropdownMenuItemType.All,
                        DropdownMenuItemType.Day,
                        DropdownMenuItemType.Week,
                        DropdownMenuItemType.Month,
                        DropdownMenuItemType.Year,
                    ),
                    selected = DropdownMenuItemType.Day,
                    expanded = false,
                ),
                selectedArchive = HitsArchiveState(year = 2026, month = 1),
            ),
            actions = NoOpHitsScreenActions,
            lazyListState = rememberLazyListState(),
        )
    }
}
