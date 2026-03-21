package pl.masslany.podkop.features.observed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.components.DropdownMenu
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.observed.preview.NoOpObservedActions
import pl.masslany.podkop.features.observed.preview.ObservedScreenStateProvider
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_comment
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.observed_discussion_entry_banner
import podkop.composeapp.generated.resources.observed_discussion_link_banner
import podkop.composeapp.generated.resources.observed_empty_discussions
import podkop.composeapp.generated.resources.observed_empty_everything
import podkop.composeapp.generated.resources.observed_empty_profiles
import podkop.composeapp.generated.resources.observed_empty_tags
import podkop.composeapp.generated.resources.topbar_label_observed

private const val FabItemsOffset = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ObservedScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ObservedViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = "${state.screenInstanceId}:${state.selectedType}",
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    ObservedScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservedScreenContent(
    paddingValues: PaddingValues,
    state: ObservedScreenState,
    actions: ObservedActions,
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

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_observed))
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
                        ObservedScreenList(
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
private fun ObservedScreenList(
    state: ObservedScreenState,
    actions: ObservedActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = 16.dp +
                WindowInsets
                    .navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding(),
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
            items = state.items,
            key = { item -> item.key },
            contentType = { item -> item.resource.contentType },
        ) {
            ObservedResourceItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                item = it,
                actions = actions,
            )
        }

        if (state.items.isEmpty()) {
            item(key = "EmptyState") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(resource = observedEmptyMessageRes(state.selectedType)),
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
private fun ObservedResourceItem(
    item: ObservedListItemState,
    actions: ObservedActions,
    modifier: Modifier = Modifier,
) {
    val discussionBanner = item.discussionBanner
    if (discussionBanner == null) {
        ResourceItemRenderer(
            modifier = modifier,
            state = item.resource,
            actions = actions,
            config = ResourceItemConfig(
                showReplyAction = true,
            ),
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_comment),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = observedDiscussionBannerText(discussionBanner = discussionBanner),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            )
            ResourceItemRenderer(
                state = item.resource,
                actions = actions,
                config = ResourceItemConfig(
                    showReplyAction = true,
                ),
            )
        }
    }
}

@Composable
private fun observedDiscussionBannerText(discussionBanner: ObservedDiscussionBannerState): String =
    when (discussionBanner.type) {
        ObservedDiscussionBannerType.Entry -> pluralStringResource(
            resource = Res.plurals.observed_discussion_entry_banner,
            quantity = discussionBanner.newContentCount,
            discussionBanner.newContentCount,
        )

        ObservedDiscussionBannerType.Link -> pluralStringResource(
            resource = Res.plurals.observed_discussion_link_banner,
            quantity = discussionBanner.newContentCount,
            discussionBanner.newContentCount,
        )
    }

internal fun observedEmptyMessageRes(type: ObservedType): StringResource = when (type) {
    ObservedType.All -> Res.string.observed_empty_everything
    ObservedType.Profiles -> Res.string.observed_empty_profiles
    ObservedType.Discussions -> Res.string.observed_empty_discussions
    ObservedType.Tags -> Res.string.observed_empty_tags
}

@Preview
@Composable
private fun ObservedScreenContentPreview(
    @PreviewParameter(ObservedScreenStateProvider::class) state: ObservedScreenState,
) {
    PodkopPreview(darkTheme = false) {
        ObservedScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpObservedActions,
            lazyListState = rememberLazyListState(),
        )
    }
}
