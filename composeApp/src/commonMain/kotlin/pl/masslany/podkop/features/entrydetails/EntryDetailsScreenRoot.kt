package pl.masslany.podkop.features.entrydetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.nestedScrollConnection
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.common.theme.colorsPalette
import pl.masslany.podkop.features.entrydetails.preview.EntryDetailsScreenStateProvider
import pl.masslany.podkop.features.entrydetails.preview.NoOpEntryDetailsActions
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_profile
import podkop.composeapp.generated.resources.entry_details_screen_error_loading_comments
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_person
import podkop.composeapp.generated.resources.topbar_label_entry

private const val FAB_ITEMS_OFFSET = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntryDetailsScreenRoot(
    screen: EntryDetailsScreen,
    paddingValues: PaddingValues,
    showTopBar: Boolean = true,
) {
    val viewModel = koinViewModel<EntryDetailsViewModel>(
        parameters = { parametersOf(screen) },
    )
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    EntryDetailsScreenContent(
        paddingValues = paddingValues,
        showTopBar = showTopBar,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailsScreenContent(
    paddingValues: PaddingValues,
    showTopBar: Boolean,
    state: EntryDetailsScreenState,
    actions: EntryDetailsActions,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
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
    val coroutineScope = rememberCoroutineScope()
    val scaffoldModifier = modifier
        .padding(
            top = if (!showTopBar) paddingValues.calculateTopPadding() else 0.dp,
            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
        )
        .fillMaxSize()
        .let { baseModifier ->
            if (showTopBar) {
                baseModifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            } else {
                baseModifier
                    .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection())
            }
        }

    Scaffold(
        modifier = scaffoldModifier,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                resource = Res.string.topbar_label_entry,
                            ),
                        )
                    },
                    actions = {
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
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPaddingValues.calculateTopPadding()),
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = actions::onRefresh,
                modifier = Modifier.fillMaxSize(),
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
                    EntryDetailsScreenList(
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
private fun EntryDetailsScreenList(
    modifier: Modifier = Modifier,
    state: EntryDetailsScreenState,
    actions: EntryDetailsActions,
    lazyListState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = WindowInsets
                .systemBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp,
        ),
    ) {
        val replyActionsConfig = ResourceItemConfig(
            showReplyAction = true,
            isReplyActionEnabled = state.isLoggedIn,
            renderEntryAsCard = true,
        )

        if (state.entry != null) {
            item(
                key = "Entry",
            ) {
                Column {
                    ResourceItemRenderer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        state = state.entry,
                        actions = actions,
                        config = replyActionsConfig,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (state.isCommentsError) {
            item(
                key = "CommentsContextError",
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(resource = Res.string.entry_details_screen_error_loading_comments),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        itemsIndexed(
            items = state.comments,
            key = { _, item -> item.id },
            contentType = { _, item -> item.contentType },
        ) { index, item ->
            val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val authorColor = MaterialTheme.colorsPalette.nameGreen
            val isCurrentUserComment = (item as? EntryCommentItemState)
                ?.authorState
                ?.name == state.currentUsername

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .drawBehind {
                        val lineWidth = 2.dp.toPx()
                        drawRect(
                            color = if (isCurrentUserComment && !state.currentUsername.isNullOrBlank()) {
                                authorColor
                            } else {
                                lineColor
                            },
                            topLeft = Offset(0f, 0f),
                            size = Size(lineWidth, size.height),
                        )
                    },
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(Modifier.width(2.dp))

                    ResourceItemRenderer(
                        state = item,
                        actions = actions,
                        config = replyActionsConfig,
                    )
                }

                if (index != state.comments.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            start = 16.dp,
                        ),
                    )
                }
            }
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
private fun EntryDetailsScreenContentPreview(
    @PreviewParameter(EntryDetailsScreenStateProvider::class) state: EntryDetailsScreenState,
) {
    PodkopPreview(darkTheme = false) {
        EntryDetailsScreenContent(
            paddingValues = PaddingValues(),
            showTopBar = true,
            state = state,
            actions = NoOpEntryDetailsActions,
            lazyListState = rememberLazyListState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
