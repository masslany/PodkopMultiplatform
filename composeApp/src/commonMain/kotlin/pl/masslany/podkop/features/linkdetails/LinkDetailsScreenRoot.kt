package pl.masslany.podkop.features.linkdetails

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
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
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.nestedScrollConnection
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.linkdetails.components.LinkDetailsHeader
import pl.masslany.podkop.features.linkdetails.components.RelatedItem
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
import pl.masslany.podkop.features.linkdetails.preview.LinkDetailsScreenStateProvider
import pl.masslany.podkop.features.linkdetails.preview.NoOpLinkDetailsActions
import pl.masslany.podkop.features.resources.components.LinkCommentItem
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_profile
import podkop.composeapp.generated.resources.comment_button_load_all_comments
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_person
import podkop.composeapp.generated.resources.links_details_screen_error_loading_comments
import podkop.composeapp.generated.resources.links_details_screen_error_loading_related
import podkop.composeapp.generated.resources.links_details_screen_no_comments
import podkop.composeapp.generated.resources.links_details_screen_no_related
import podkop.composeapp.generated.resources.links_details_screen_related_label

private const val FAB_ITEMS_OFFSET = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkDetailsScreenRoot(
    id: Int,
    paddingValues: PaddingValues,
    showTopBar: Boolean = true,
) {
    val viewModel = koinViewModel<LinkDetailsViewModel>(
        parameters = { parametersOf(id) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    LinkDetailsScreenContent(
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
fun LinkDetailsScreenContent(
    paddingValues: PaddingValues,
    showTopBar: Boolean,
    state: LinkDetailsScreenState,
    actions: LinkDetailsActions,
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
    val showTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldModifier = modifier
        .padding(
            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
        )
        .fillMaxSize()
        .let { baseModifier ->
            if (showTopBar) {
                baseModifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
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
                        if (showTitle) {
                            Text(
                                text = state.link?.titleState?.title.orEmpty(),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleSmall,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
                    LinkDetailsScreenList(
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
private fun LinkDetailsScreenList(
    modifier: Modifier = Modifier,
    state: LinkDetailsScreenState,
    actions: LinkDetailsActions,
    lazyListState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            bottom = WindowInsets
                .systemBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp,
        ),
    ) {
        state.link?.let { link ->
            item(
                key = "LinkDetailsHeader",
            ) {
                LinkDetailsHeader(
                    state = link,
                    isReplyEnabled = state.isLoggedIn,
                    onLinkClick = { actions.onLinkUrlClicked(link.sourceUrl) },
                    onVoteClick = { actions.onLinkVoteClicked(link.id, link.countState.isVoted) },
                    onFavouriteClick = {
                        actions.onLinkFavouriteClicked(
                            linkId = link.id,
                            favourited = link.isFavourite,
                        )
                    },
                    onAuthorClick = { actions.onProfileClicked(it) },
                    onTagClick = { actions.onTagClicked(it) },
                    onReplyClick = {
                        actions.onLinkReplyClicked(
                            linkId = link.id,
                            author = null,
                        )
                    },
                    onMoreClick = {
                        actions.onLinkMoreClicked(
                            linkId = link.id,
                            linkSlug = link.slug,
                        )
                    },
                )
            }
        }

        item(
            key = "RelatedHeader",
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(resource = Res.string.links_details_screen_related_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        when (val relatedState = state.relatedState) {
            LinkDetailsRelatedState.Loading -> {
                item(
                    key = "RelatedLoading",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            LinkDetailsRelatedState.Error -> {
                item(
                    key = "RelatedError",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center),
                            text = stringResource(resource = Res.string.links_details_screen_error_loading_related),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            LinkDetailsRelatedState.Empty -> {
                item(
                    key = "RelatedEmpty",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center),
                            text = stringResource(resource = Res.string.links_details_screen_no_related),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            is LinkDetailsRelatedState.Content -> {
                item(
                    key = "RelatedContent",
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        items(
                            items = relatedState.items,
                            key = { item -> item.id },
                            contentType = { item -> item.contentType },
                        ) { relatedItem ->
                            RelatedItem(
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillParentMaxWidth(),
                                state = relatedItem,
                                onItemClick = { actions.onLinkUrlClicked(relatedItem.sourceUrl) },
                                onAuthorClick = { actions.onProfileClicked(it) },
                                onSourceClick = { actions.onLinkUrlClicked(relatedItem.sourceUrl) },
                                onVoteClick = {
                                    actions.onLinkVoteClicked(
                                        id = relatedItem.id,
                                        voted = relatedItem.voteState
                                            ?.positiveVoteButtonState
                                            ?.isVoted
                                            ?: false,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        when (val commentsState = state.commentsState) {
            is LinkDetailsCommentsState.Loading -> {
                item(
                    key = "CommentsLoading",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            is LinkDetailsCommentsState.Error -> {
                item(
                    key = "CommentsError",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center),
                            text = stringResource(resource = Res.string.links_details_screen_error_loading_comments),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            is LinkDetailsCommentsState.Empty -> {
                item(
                    key = "CommentsDropdown",
                ) {
                    DropdownMenu(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = commentsState.sortMenuState.items,
                        selected = commentsState.sortMenuState.selected,
                        expanded = commentsState.sortMenuState.expanded,
                        onSelected = actions::onSortSelected,
                        onExpandedChange = actions::onSortExpandedChanged,
                        onDismissRequest = actions::onSortDismissed,
                    )
                }

                item(
                    key = "CommentsEmpty",
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center),
                            text = stringResource(resource = Res.string.links_details_screen_no_comments),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            is LinkDetailsCommentsState.Content -> {
                item(
                    key = "CommentsDropdown",
                ) {
                    DropdownMenu(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = commentsState.sortMenuState.items,
                        selected = commentsState.sortMenuState.selected,
                        expanded = commentsState.sortMenuState.expanded,
                        onSelected = actions::onSortSelected,
                        onExpandedChange = actions::onSortExpandedChanged,
                        onDismissRequest = actions::onSortDismissed,
                    )
                }

                items(
                    items = commentsState.comments,
                    key = { item -> item.id },
                ) { comment ->
                    LinkDetailsCommentItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        state = comment,
                        actions = actions,
                        isReplyEnabled = state.isLoggedIn,
                    )
                }

                if (commentsState.isPaginating) {
                    item(
                        key = "CommentsPaginating",
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkDetailsCommentItem(
    modifier: Modifier = Modifier,
    state: LinkDetailsCommentItemState,
    actions: LinkDetailsActions,
    isReplyEnabled: Boolean,
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                LinkCommentItem(
                    modifier = Modifier.fillMaxWidth(),
                    state = state.comment,
                    isReplyEnabled = isReplyEnabled,
                    onProfileClick = { actions.onProfileClicked(it) },
                    onTagClick = { actions.onTagClicked(it) },
                    onUrlClick = { actions.onLinkUrlClicked(it) },
                    onVoteUpClick = {
                        actions.onLinkCommentVoteUpClick(
                            linkId = state.comment.linkId,
                            commentId = state.comment.id,
                            voted = state.comment.voteState.positiveVoteButtonState?.isVoted ?: false,
                        )
                    },
                    onFavouriteClick = {
                        actions.onLinkCommentFavouriteClicked(
                            linkId = state.comment.linkId,
                            commentId = state.comment.id,
                            favourited = state.comment.isFavourite,
                        )
                    },
                    onImageClick = { actions.onImageClicked(it) },
                    onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.comment.id, embed) },
                    onMoreClick = {
                        actions.onLinkCommentMoreClicked(
                            linkId = state.comment.linkId,
                            commentId = state.comment.id,
                            linkSlug = state.comment.linkSlug,
                            parentCommentId = state.comment.parentCommentIdOrNull,
                        )
                    },
                    onReplyClick = {
                        actions.onLinkCommentReplyClicked(
                            linkId = state.comment.linkId,
                            commentId = state.comment.id,
                            author = state.comment.authorState?.name,
                        )
                    },
                )

                state.replies.forEach { reply ->
                    Spacer(Modifier.size(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Spacer(Modifier.size(8.dp))
                    LinkCommentItem(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .fillMaxWidth(),
                        state = reply,
                        isReplyEnabled = isReplyEnabled,
                        onProfileClick = { actions.onProfileClicked(it) },
                        onTagClick = { actions.onTagClicked(it) },
                        onUrlClick = { actions.onLinkUrlClicked(it) },
                        onVoteUpClick = {
                            actions.onLinkCommentVoteUpClick(
                                linkId = reply.linkId,
                                commentId = reply.id,
                                voted = reply.voteState.positiveVoteButtonState?.isVoted ?: false,
                            )
                        },
                        onFavouriteClick = {
                            actions.onLinkCommentFavouriteClicked(
                                linkId = reply.linkId,
                                commentId = reply.id,
                                favourited = reply.isFavourite,
                            )
                        },
                        onImageClick = { actions.onImageClicked(it) },
                        onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(reply.id, embed) },
                        onMoreClick = {
                            actions.onLinkCommentMoreClicked(
                                linkId = reply.linkId,
                                commentId = reply.id,
                                linkSlug = reply.linkSlug,
                                parentCommentId = reply.parentCommentIdOrNull,
                            )
                        },
                        onReplyClick = {
                            actions.onLinkCommentReplyClicked(
                                linkId = reply.linkId,
                                commentId = reply.id,
                                author = reply.authorState?.name,
                            )
                        },
                    )
                }

                state.nextRepliesPage?.let { nextPage ->
                    Spacer(Modifier.size(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        OutlinedButton(
                            onClick = {
                                actions.onShowMoreRepliesClicked(
                                    commentId = state.id,
                                    nextPage = nextPage,
                                )
                            },
                            enabled = !state.isLoadingReplies,
                        ) {
                            if (state.isLoadingReplies) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.size(8.dp))
                            }
                            Text(
                                text = stringResource(
                                    resource = Res.string.comment_button_load_all_comments,
                                    state.remainingRepliesCount,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LinkDetailsScreenContentPreview(
    @PreviewParameter(LinkDetailsScreenStateProvider::class) state: LinkDetailsScreenState,
) {
    PodkopPreview(darkTheme = false) {
        LinkDetailsScreenContent(
            paddingValues = PaddingValues(),
            showTopBar = true,
            state = state,
            actions = NoOpLinkDetailsActions,
            lazyListState = rememberLazyListState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
