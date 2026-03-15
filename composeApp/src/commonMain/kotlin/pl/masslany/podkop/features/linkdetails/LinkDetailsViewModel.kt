package pl.masslany.podkop.features.linkdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.embed.toTwitterEmbedPreviewState
import pl.masslany.podkop.common.models.vote.VoteReasonType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerPrefill
import pl.masslany.podkop.features.composer.ComposerRequest
import pl.masslany.podkop.features.composer.ComposerResult
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdate
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdatesStore
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogScreen
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.link.toLinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.toLinkCommentItemState
import pl.masslany.podkop.features.resources.models.related.toRelatedItemState
import pl.masslany.podkop.features.resources.updateTwitterEmbedStateIfMatches
import pl.masslany.podkop.features.topbar.TopBarActions

class LinkDetailsViewModel(
    private val id: Int,
    private val linksRepository: LinksRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val twitterEmbedPreviewRepository: TwitterEmbedPreviewRepository,
    private val appNavigator: AppNavigator,
    private val screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
    private val resourceActionUpdatesStore: ResourceActionUpdatesStore,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    LinkDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemActions by resourceItemStateHolder {

    private data class CommentRepliesState(
        val replies: ImmutableList<LinkCommentItemState>,
        val remainingRepliesCount: Int,
        val nextRepliesPage: Int?,
        val isLoadingReplies: Boolean,
    ) {
        companion object {
            val empty = CommentRepliesState(
                replies = persistentListOf(),
                remainingRepliesCount = 0,
                nextRepliesPage = null,
                isLoadingReplies = false,
            )
        }
    }

    private val availableCommentsSortTypes = linksRepository
        .getCommentsSortTypes()
        .ifEmpty { listOf(CommentsSortType.Best) }

    private var selectedCommentsSortType = availableCommentsSortTypes
        .firstOrNull { it == CommentsSortType.Best }
        ?: availableCommentsSortTypes.first()

    private var linkResource: ResourceItem? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
            appendCommentsRepliesState(data)
        },
        onError = {
            logger.error("Failed to load paginated comments for link id=$id", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        linksRepository.getComments(
            id = id,
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key.toIntOrNull()
            },
            limit = null,
            commentSortType = selectedCommentsSortType,
            ama = null,
        )
    }

    private val commentRepliesStateById = MutableStateFlow<Map<Int, CommentRepliesState>>(emptyMap())
    private val _state = MutableStateFlow(initialState())
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
        commentRepliesStateById,
    ) { state, items, paginatorState, repliesStateById ->
        val holderLink = items
            .filterIsInstance<LinkItemState>()
            .firstOrNull { it.id == id }
        val mappedComments = items
            .filterIsInstance<LinkCommentItemState>()
            .map { comment ->
                val repliesState = repliesStateById[comment.id] ?: CommentRepliesState.empty
                LinkDetailsCommentItemState(
                    id = comment.id,
                    comment = comment,
                    replies = repliesState.replies,
                    remainingRepliesCount = repliesState.remainingRepliesCount,
                    nextRepliesPage = repliesState.nextRepliesPage,
                    isLoadingReplies = repliesState.isLoadingReplies,
                )
            }
            .toImmutableList()

        state.copy(
            link = holderLink ?: state.link,
            commentsState = state.commentsState.resolve(
                comments = mappedComments,
                isPaginating = paginatorState is PaginatorState.Loading,
            ),
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), _state.value)

    init {
        resourceItemStateHolder.init(viewModelScope)
        observeResourceActionUpdates()
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    override fun onLinkDownvoteClicked(linkId: Int, isDownVoted: Boolean) {
        if (linkId != id || !state.value.isLoggedIn) return

        if (isDownVoted) {
            updateLinkVote(
                linkId = linkId,
                removeVote = true,
                downvoteReason = null,
            )
        } else {
            updateState { previousState ->
                previousState.copy(
                    downvoteMenuState = previousState.downvoteMenuState.copy(
                        expanded = true,
                    ),
                )
            }
        }
    }

    override fun onLinkDownvoteReasonSelected(linkId: Int, reason: VoteReasonType) {
        if (linkId != id || !state.value.isLoggedIn) return

        updateLinkVote(
            linkId = linkId,
            removeVote = false,
            downvoteReason = reason.toVoteReason(),
        )
    }

    override fun onLinkDownvoteDismissed() {
        updateState { previousState ->
            previousState.copy(
                downvoteMenuState = previousState.downvoteMenuState.copy(
                    expanded = false,
                ),
            )
        }
    }

    override fun onLinkReplyClicked(linkId: Int, author: String?) {
        if (linkId != id) return
        openLinkCommentComposer(
            author = author,
            parentCommentId = null,
        )
    }

    override fun onLinkCommentReplyClicked(linkId: Int, commentId: Int, author: String?) {
        if (linkId != id) return
        val replyParentCommentId = resolveReplyParentCommentId(commentId) ?: return
        openLinkCommentComposer(
            author = author,
            parentCommentId = replyParentCommentId,
        )
    }

    override fun onLinkCommentLongClicked(linkId: Int, commentId: Int) {
        if (linkId != id) return

        val commentsState = state.value.commentsState as? LinkDetailsCommentsState.Content ?: return
        val topLevelComment = commentsState.comments.firstOrNull { it.id == commentId }?.comment
        val replyComment = commentsState.comments.firstNotNullOfOrNull { item ->
            item.replies.firstOrNull {
                it.id ==
                    commentId
            }
        }
        val comment = topLevelComment ?: replyComment ?: return
        val copyContent = comment.rawContent.takeIf {
            comment.entryContentState is EntryContentState.Content &&
                !comment.isBlacklisted &&
                it.isNotBlank()
        } ?: return
        val draftId = buildLinkCommentScreenshotDraftId(commentId)

        appNavigator.navigateTo(
            ResourceTextSelectionDialogScreen(
                content = copyContent,
                previewDraftId = draftId,
            ),
        )
    }

    override fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean) {
        resourceItemStateHolder.onLinkCommentVoteUpClick(
            linkId = linkId,
            commentId = commentId,
            voted = voted,
        )
        commentRepliesStateById.update { previousState ->
            previousState.mapValues { (_, itemState) ->
                itemState.copy(
                    replies = itemState.replies.applyVoteById(
                        commentId = commentId,
                        isRemovingVote = voted,
                        direction = LinkCommentVoteDirection.Up,
                    ),
                )
            }
        }
    }

    override fun onLinkCommentVoteDownClick(linkId: Int, commentId: Int, voted: Boolean) {
        resourceItemStateHolder.onLinkCommentVoteDownClick(
            linkId = linkId,
            commentId = commentId,
            voted = voted,
        )
        commentRepliesStateById.update { previousState ->
            previousState.mapValues { (_, itemState) ->
                itemState.copy(
                    replies = itemState.replies.applyVoteById(
                        commentId = commentId,
                        isRemovingVote = voted,
                        direction = LinkCommentVoteDirection.Down,
                    ),
                )
            }
        }
    }

    override fun onLinkCommentFavouriteClicked(linkId: Int, commentId: Int, favourited: Boolean) {
        resourceItemStateHolder.onLinkCommentFavouriteClicked(
            linkId = linkId,
            commentId = commentId,
            favourited = favourited,
        )
        commentRepliesStateById.update { previousState ->
            previousState.mapValues { (_, itemState) ->
                itemState.copy(
                    replies = itemState.replies.applyFavouriteById(
                        commentId = commentId,
                        favourited = favourited,
                    ),
                )
            }
        }
    }

    override fun onEmbedPreviewClicked(itemId: Int, state: EmbedContentState) {
        if (!hasLoadedReplyWithId(itemId)) {
            resourceItemStateHolder.onEmbedPreviewClicked(itemId, state)
            return
        }

        when (state.type) {
            EmbedContentType.Twitter -> {
                when (state.twitterState) {
                    TwitterEmbedState.Preview -> {
                        viewModelScope.launch {
                            updateReplyTwitterEmbedState(
                                itemId = itemId,
                                embedKey = state.key,
                                newState = TwitterEmbedState.Loading,
                            )

                            twitterEmbedPreviewRepository.getTweet(state.url)
                                .onSuccess { tweet ->
                                    updateReplyTwitterEmbedState(
                                        itemId = itemId,
                                        embedKey = state.key,
                                        newState = TwitterEmbedState.Loaded(
                                            tweet.toTwitterEmbedPreviewState(),
                                        ),
                                    )
                                }
                                .onFailure { error ->
                                    logger.error(
                                        message = "Twitter embed preview fetch failed for reply itemId=$itemId " +
                                            "url=${state.url}",
                                        throwable = error,
                                    )
                                    updateReplyTwitterEmbedState(
                                        itemId = itemId,
                                        embedKey = state.key,
                                        newState = TwitterEmbedState.Error,
                                    )
                                }
                        }
                    }

                    TwitterEmbedState.Loading -> Unit

                    null,
                    is TwitterEmbedState.Loaded,
                    TwitterEmbedState.Error,
                    -> resourceItemStateHolder.onEmbedPreviewClicked(itemId, state)
                }
            }

            else -> resourceItemStateHolder.onEmbedPreviewClicked(itemId, state)
        }
    }

    override fun onLinkCommentMoreClicked(
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    ) {
        val draftId = buildLinkCommentScreenshotDraftId(commentId)
        val commentsState = state.value.commentsState as? LinkDetailsCommentsState.Content
        val topLevelComment = commentsState
            ?.comments
            ?.firstOrNull { it.id == commentId }
            ?.comment
        val replyComment = commentsState
            ?.comments
            ?.firstNotNullOfOrNull { item -> item.replies.firstOrNull { it.id == commentId } }
        val targetComment = topLevelComment ?: replyComment
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forLinkComment(
                linkId = linkId,
                linkSlug = linkSlug,
                linkCommentId = commentId,
                parentCommentId = parentCommentId,
                screenshotDraftId = draftId,
                canEdit = targetComment?.isEditEnabled == true,
                content = targetComment?.rawContent.orEmpty(),
                copyContent = targetComment
                    ?.takeIf { it.entryContentState is EntryContentState.Content && !it.isBlacklisted }
                    ?.rawContent
                    ?.takeIf { it.isNotBlank() },
                adult = targetComment?.adult == true,
                photoKey = targetComment?.embedImageState?.key,
                photoUrl = targetComment?.embedImageState?.url,
            ),
        )
    }

    override fun onLinkMoreClicked(linkId: Int, linkSlug: String) {
        if (linkId != id) return
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forLink(
                linkId = linkId,
                linkSlug = linkSlug,
            ),
        )
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        selectedCommentsSortType = sortType.toCommentsSortType()
        updateState { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.updateSortMenuState { menu ->
                    menu.copy(
                        selected = sortType,
                        expanded = false,
                    )
                }.toLoading(),
            )
        }
        loadCommentsPageOne()
    }

    override fun onSortExpandedChanged(expanded: Boolean) {
        updateState { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.updateSortMenuState { menu ->
                    menu.copy(expanded = expanded)
                },
            )
        }
    }

    override fun onSortDismissed() {
        updateState { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.updateSortMenuState { menu ->
                    menu.copy(expanded = false)
                },
            )
        }
    }

    override fun onShowMoreRepliesClicked(commentId: Int, nextPage: Int) {
        val parentLinkSlug = (state.value.commentsState as? LinkDetailsCommentsState.Content)
            ?.comments
            ?.firstOrNull { it.id == commentId }
            ?.comment
            ?.linkSlug

        commentRepliesStateById.update { previousState ->
            val current = previousState[commentId] ?: CommentRepliesState.empty
            previousState + (commentId to current.copy(isLoadingReplies = true))
        }

        viewModelScope.launch {
            linksRepository.getSubComments(
                linkId = id,
                commentId = commentId,
                page = nextPage,
            ).onSuccess { resources ->
                val loadedReplies = resources.data
                    .map {
                        it.toLinkCommentItemState(
                            linkIdOverride = id,
                            linkSlugOverride = parentLinkSlug ?: it.slug,
                        )
                    }

                commentRepliesStateById.update { previousState ->
                    val current = previousState[commentId] ?: CommentRepliesState.empty
                    previousState + (
                        commentId to current.mergeReplies(
                            replies = loadedReplies,
                            pagination = resources.pagination,
                            requestedPage = nextPage,
                        )
                        )
                }
            }.onFailure {
                logger.error(
                    "Failed to load replies for link id=$id, commentId=$commentId, page=$nextPage",
                    it,
                )
                commentRepliesStateById.update { previousState ->
                    val current = previousState[commentId] ?: return@update previousState
                    previousState + (commentId to current.copy(isLoadingReplies = false))
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onRelatedVoteUpClicked(relatedId: Int, voted: Boolean) {
        updateRelatedVote(
            relatedId = relatedId,
            voted = voted,
            direction = RelatedLinkVoteDirection.Up,
        )
    }

    override fun onRelatedVoteDownClicked(relatedId: Int, voted: Boolean) {
        updateRelatedVote(
            relatedId = relatedId,
            voted = voted,
            direction = RelatedLinkVoteDirection.Down,
        )
    }

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean {
        if (state.value.commentsState !is LinkDetailsCommentsState.Content) {
            return false
        }
        return paginator.shouldPaginate(lastVisibleIndex, totalItems)
    }

    override fun paginate() {
        if (state.value.commentsState !is LinkDetailsCommentsState.Content) {
            return
        }
        paginator.paginate()
    }

    override fun onLinkVoteClicked(id: Int, voted: Boolean) {
        updateLinkVote(
            linkId = id,
            removeVote = voted,
            downvoteReason = null,
        )
    }

    private fun loadContent(isRefreshing: Boolean) {
        updateState { previousState ->
            previousState.copy(
                isLoading = !isRefreshing,
                isError = false,
                isRefreshing = isRefreshing,
                commentsState = previousState.commentsState.toLoading(),
                relatedState = LinkDetailsRelatedState.Loading,
            )
        }

        viewModelScope.launch {
            coroutineScope {
                val viewerContextDeferred = async {
                    resolveViewerContext()
                }
                val linkDeferred = async {
                    linksRepository.getLink(id)
                }
                val commentsDeferred = async {
                    linksRepository.getComments(
                        id = id,
                        page = 1,
                        limit = null,
                        commentSortType = selectedCommentsSortType,
                        ama = null,
                    )
                }
                val relatedDeferred = async {
                    linksRepository.getRelatedLinks(linkId = id)
                }

                val isLinkLoaded = linkDeferred.await()
                    .onSuccess { link ->
                        linkResource = link.data
                        updateState { previousState ->
                            previousState.copy(
                                link = link.data.toLinkItemState(isUpcoming = false),
                            ).syncDownvoteVisibility()
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load link details for id=$id", it)
                    }
                    .isSuccess

                viewerContextDeferred.await().let { viewerContext ->
                    updateState { previousState ->
                        previousState.copy(
                            isLoggedIn = viewerContext.isLoggedIn,
                            currentUsername = viewerContext.username,
                        ).syncDownvoteVisibility()
                    }
                }

                commentsDeferred.await().onSuccess { comments ->
                    onCommentsPageOneLoaded(comments)
                }.onFailure {
                    logger.error("Failed to load comments for link id=$id", it)
                    onCommentsPageOneLoadFailed()
                    snackbarManager.tryEmitGenericError()
                }

                relatedDeferred.await()
                    .onSuccess { related ->
                        updateState { previousState ->
                            previousState.copy(
                                relatedState = related.toRelatedState(),
                            )
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load related links for link id=$id", it)
                        updateState { previousState ->
                            previousState.copy(
                                relatedState = LinkDetailsRelatedState.Error,
                            )
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                updateState { previousState ->
                    previousState.copy(
                        isError = !isLinkLoaded,
                    ).syncDownvoteVisibility()
                }
            }

            updateState { previousState ->
                previousState.copy(
                    isLoading = false,
                    isRefreshing = false,
                ).syncDownvoteVisibility()
            }
        }
    }

    private fun updateRelatedVote(
        relatedId: Int,
        voted: Boolean,
        direction: RelatedLinkVoteDirection,
    ) {
        viewModelScope.launch {
            val result = when {
                voted -> linksRepository.removeVoteOnRelatedLink(linkId = id, relatedId = relatedId)

                direction == RelatedLinkVoteDirection.Up ->
                    linksRepository.voteUpOnRelatedLink(linkId = id, relatedId = relatedId)

                else -> linksRepository.voteDownOnRelatedLink(linkId = id, relatedId = relatedId)
            }

            result.onSuccess {
                refreshRelatedLinks()
            }.onFailure {
                logger.error(
                    "Failed to update related vote for link id=$id, relatedId=$relatedId, direction=$direction, remove=$voted",
                    it,
                )
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private fun updateLinkVote(
        linkId: Int,
        removeVote: Boolean,
        downvoteReason: VoteReason?,
    ) {
        if (linkId != id) return
        if (downvoteReason != null && state.value.downvoteMenuState.isSubmitting) return

        viewModelScope.launch {
            if (downvoteReason != null) {
                updateState { previousState ->
                    previousState.copy(
                        downvoteMenuState = previousState.downvoteMenuState.copy(
                            isSubmitting = true,
                        ),
                    )
                }
            }

            val voteResult = when {
                removeVote -> linksRepository.removeVoteOnLink(linkId = linkId)
                downvoteReason != null -> linksRepository.voteDownOnLink(linkId = linkId, reason = downvoteReason)
                else -> linksRepository.voteOnLink(linkId = linkId)
            }

            voteResult.onSuccess {
                updateState { previousState ->
                    previousState.copy(
                        downvoteMenuState = LinkDownvoteMenuState.initial,
                    )
                }
                refreshLink(linkId)
            }.onFailure {
                logger.error(
                    "Failed to update link vote for id=$linkId removeVote=$removeVote downvoteReason=$downvoteReason",
                    it,
                )
                updateState { previousState ->
                    previousState.copy(
                        downvoteMenuState = previousState.downvoteMenuState.copy(
                            isSubmitting = false,
                        ),
                    )
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private fun observeResourceActionUpdates() {
        viewModelScope.launch {
            resourceActionUpdatesStore.updates.collect { update ->
                when (update) {
                    is ResourceActionUpdate.ResourceEdited -> patchReplyWithEditedResource(update.resource)
                    else -> Unit
                }
            }
        }
    }

    private fun patchReplyWithEditedResource(resource: ResourceItem) {
        commentRepliesStateById.update { previousState ->
            previousState.mapValues { (_, itemState) ->
                itemState.copy(
                    replies = itemState.replies.patchEditedResource(resource),
                )
            }
        }
    }

    private fun loadCommentsPageOne() {
        viewModelScope.launch {
            linksRepository.getComments(
                id = id,
                page = 1,
                limit = null,
                commentSortType = selectedCommentsSortType,
                ama = null,
            ).onSuccess { comments ->
                onCommentsPageOneLoaded(comments)
            }.onFailure {
                logger.error("Failed to load comments page one for link id=$id", it)
                onCommentsPageOneLoadFailed()
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private suspend fun onCommentsPageOneLoaded(comments: Resources) {
        resourceItemStateHolder.updateData(topLevelLinkAndComments(comments.data))
        replaceCommentsRepliesState(comments.data)
        paginator.setup(
            pagination = comments.pagination,
            initialItemCount = comments.data.size,
        )
        updateState { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.toLoaded(),
            )
        }
    }

    private suspend fun onCommentsPageOneLoadFailed() {
        resourceItemStateHolder.updateData(topLevelLinkAndComments(emptyList()))
        commentRepliesStateById.value = emptyMap()
        updateState { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.toError(),
            )
        }
    }

    private suspend fun refreshLink(linkId: Int) {
        linksRepository.getLink(linkId)
            .onSuccess { link ->
                linkResource = link.data
                resourceItemStateHolder.notifyItemUpdated(link.data)
                val updatedState = link.data.toLinkItemState(isUpcoming = false)
                val updatedRelatedState = link.data.toRelatedItemState()
                updateState { previousState ->
                    previousState.copy(
                        link = if (previousState.link?.id == linkId) {
                            updatedState
                        } else {
                            previousState.link
                        },
                        downvoteMenuState = LinkDownvoteMenuState.initial,
                        relatedState = when (val relatedState = previousState.relatedState) {
                            is LinkDetailsRelatedState.Content -> {
                                relatedState.copy(
                                    items = relatedState.items.map { related ->
                                        if (related.id == linkId) {
                                            updatedRelatedState
                                        } else {
                                            related
                                        }
                                    }.toImmutableList(),
                                )
                            }

                            else -> relatedState
                        },
                    ).syncDownvoteVisibility()
                }
            }
    }

    private suspend fun refreshRelatedLinks() {
        linksRepository.getRelatedLinks(linkId = id)
            .onSuccess { related ->
                updateState { previousState ->
                    previousState.copy(
                        relatedState = related.toRelatedState(),
                    )
                }
            }
            .onFailure {
                logger.error("Failed to refresh related links for link id=$id", it)
                snackbarManager.tryEmitGenericError()
            }
    }

    private fun replaceCommentsRepliesState(items: List<ResourceItem>) {
        commentRepliesStateById.value = buildInitialCommentRepliesState(items)
    }

    private fun appendCommentsRepliesState(items: List<ResourceItem>) {
        val newState = buildInitialCommentRepliesState(items)
        commentRepliesStateById.update { previousState ->
            previousState + newState.filterKeys { key -> !previousState.containsKey(key) }
        }
    }

    private fun topLevelLinkAndComments(
        comments: List<ResourceItem>,
    ): List<ResourceItem> {
        val topLevel = linkResource
        return if (topLevel == null) {
            comments
        } else {
            buildList(comments.size + 1) {
                add(topLevel)
                addAll(comments)
            }
        }
    }

    private suspend fun appendCreatedComment(
        createdComment: ResourceItem,
        repliedCommentId: Int?,
    ) {
        val targetTopLevelCommentId = createdComment.parentId ?: repliedCommentId
        val hasTargetTopLevelComment = targetTopLevelCommentId != null &&
            (
                (state.value.commentsState as? LinkDetailsCommentsState.Content)
                    ?.comments
                    ?.any { it.id == targetTopLevelCommentId } == true
                )

        if (!hasTargetTopLevelComment) {
            resourceItemStateHolder.appendData(listOf(createdComment))
            appendCommentsRepliesState(listOf(createdComment))
            return
        }

        val parentLinkSlug = findTopLevelCommentById(targetTopLevelCommentId)?.comment?.linkSlug
        val mappedReply = createdComment.toLinkCommentItemState(
            linkIdOverride = id,
            linkSlugOverride = parentLinkSlug ?: createdComment.slug,
        )

        commentRepliesStateById.update { previousState ->
            val current = previousState[targetTopLevelCommentId] ?: CommentRepliesState.empty
            previousState + (
                targetTopLevelCommentId to current.copy(
                    replies = (current.replies + mappedReply).distinctBy { it.id }.toImmutableList(),
                    remainingRepliesCount = (current.remainingRepliesCount - 1).coerceAtLeast(0),
                    nextRepliesPage = if (current.remainingRepliesCount <= 1) {
                        null
                    } else {
                        current.nextRepliesPage
                    },
                    isLoadingReplies = false,
                )
                )
        }
    }

    private fun openLinkCommentComposer(author: String?, parentCommentId: Int?) {
        if (!state.value.isLoggedIn) {
            return
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefillText = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        viewModelScope.launch {
            val resultKey = "link-details-composer-$id-${kotlin.random.Random.nextInt()}"
            val result = appNavigator.awaitResult<ComposerResult>(
                target = ComposerBottomSheetScreen(
                    resultKey = resultKey,
                    request = ComposerRequest.CreateLinkComment(
                        linkId = id,
                        parentCommentId = parentCommentId,
                        prefill = ComposerPrefill(
                            content = prefillText,
                            replyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                        ),
                    ),
                ),
                key = resultKey,
            )

            if (result is ComposerResult.Submitted) {
                appendCreatedComment(
                    createdComment = result.resource,
                    repliedCommentId = parentCommentId,
                )
            }
        }
    }

    private fun resolveReplyParentCommentId(commentId: Int): Int? {
        val topLevel = findTopLevelCommentById(commentId)
        if (topLevel != null) {
            return topLevel.id
        }

        val contentState = state.value.commentsState as? LinkDetailsCommentsState.Content ?: return null
        contentState.comments.forEach { item ->
            val matchingReply = item.replies.firstOrNull { it.id == commentId } ?: return@forEach
            return matchingReply.parentCommentIdOrNull ?: matchingReply.id
        }

        return commentId
    }

    private fun findTopLevelCommentById(commentId: Int): LinkDetailsCommentItemState? {
        val contentState = state.value.commentsState as? LinkDetailsCommentsState.Content ?: return null
        return contentState.comments.firstOrNull { it.id == commentId }
    }

    private suspend fun resolveViewerContext(): ViewerContext {
        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) {
            return ViewerContext(
                isLoggedIn = false,
                username = null,
            )
        }

        val username = profileRepository.getProfileShort()
            .onFailure {
                logger.error("Failed to resolve current profile short", it)
            }
            .getOrNull()
            ?.name

        return ViewerContext(
            isLoggedIn = true,
            username = username,
        )
    }

    private fun buildLinkCommentScreenshotDraftId(commentId: Int): String? {
        val commentsState = state.value.commentsState as? LinkDetailsCommentsState.Content ?: return null

        commentsState.comments.firstOrNull { it.id == commentId }?.let { topLevel ->
            return screenshotShareDraftStore.put(
                ResourceScreenshotShareDraft.LinkComment(
                    comment = topLevel.comment,
                ),
            )
        }

        commentsState.comments.forEach { item ->
            val reply = item.replies.firstOrNull { it.id == commentId } ?: return@forEach
            return screenshotShareDraftStore.put(
                ResourceScreenshotShareDraft.LinkComment(
                    comment = reply,
                    parentComment = item.comment,
                ),
            )
        }

        return null
    }

    private fun buildInitialCommentRepliesState(
        items: List<ResourceItem>,
    ): Map<Int, CommentRepliesState> = items.associate { item ->
        val replies = item.comments
            ?.items
            .orEmpty()
            .map { comment -> comment.toLinkCommentItemState(linkId = id, linkSlug = item.slug) }
            .toImmutableList()
        val totalReplies = item.comments?.count ?: replies.size

        item.id to CommentRepliesState(
            replies = replies,
            remainingRepliesCount = (totalReplies - replies.size).coerceAtLeast(0),
            nextRepliesPage = if (replies.size < totalReplies) 1 else null,
            isLoadingReplies = false,
        )
    }

    private inline fun updateState(transform: (LinkDetailsScreenState) -> LinkDetailsScreenState) {
        _state.update { previousState ->
            transform(previousState)
        }
    }

    private fun updateReplyTwitterEmbedState(
        itemId: Int,
        embedKey: String,
        newState: TwitterEmbedState,
    ) {
        commentRepliesStateById.update { previousState ->
            previousState.mapValues { (_, repliesState) ->
                repliesState.copy(
                    replies = repliesState.replies.applyTwitterEmbedStateById(
                        commentId = itemId,
                        embedKey = embedKey,
                        newState = newState,
                    ),
                )
            }
        }
    }

    private fun hasLoadedReplyWithId(commentId: Int): Boolean =
        commentRepliesStateById.value.values.any { repliesState ->
            repliesState.replies.containsCommentWithId(commentId)
        }

    private fun initialState(): LinkDetailsScreenState = LinkDetailsScreenState.initial.copy(
        commentsState = LinkDetailsCommentsState.Loading(
            sortMenuState = DropdownMenuState(
                items = availableCommentsSortTypes
                    .map { it.toDropdownMenuItemType() }
                    .toImmutableList(),
                selected = selectedCommentsSortType.toDropdownMenuItemType(),
                expanded = false,
            ),
        ),
    )

    private fun CommentRepliesState.mergeReplies(
        replies: List<LinkCommentItemState>,
        pagination: Pagination?,
        requestedPage: Int,
    ): CommentRepliesState {
        val shouldReplaceExisting = requestedPage == 1 &&
            this.replies.size < (pagination?.perPage ?: Int.MAX_VALUE)

        val mergedReplies = if (shouldReplaceExisting) {
            replies
        } else {
            (this.replies + replies).distinctBy { it.id }
        }.toImmutableList()

        val totalReplies = pagination?.total ?: (mergedReplies.size + this.remainingRepliesCount)
        val remainingReplies = (totalReplies - mergedReplies.size).coerceAtLeast(0)

        return copy(
            replies = mergedReplies,
            remainingRepliesCount = remainingReplies,
            nextRepliesPage = if (remainingReplies > 0) requestedPage + 1 else null,
            isLoadingReplies = false,
        )
    }

    private data class ViewerContext(val isLoggedIn: Boolean, val username: String?)
}

private fun LinkDetailsCommentsState.resolve(
    comments: ImmutableList<LinkDetailsCommentItemState>,
    isPaginating: Boolean,
): LinkDetailsCommentsState = when (this) {
    is LinkDetailsCommentsState.Loading -> this

    is LinkDetailsCommentsState.Error -> this

    is LinkDetailsCommentsState.Empty,
    is LinkDetailsCommentsState.Content,
    -> {
        if (comments.isEmpty()) {
            LinkDetailsCommentsState.Empty(sortMenuState = this.sortMenuState)
        } else {
            LinkDetailsCommentsState.Content(
                sortMenuState = this.sortMenuState,
                comments = comments,
                isPaginating = isPaginating,
            )
        }
    }
}

private fun LinkDetailsCommentsState.updateSortMenuState(
    update: (DropdownMenuState) -> DropdownMenuState,
): LinkDetailsCommentsState = when (this) {
    is LinkDetailsCommentsState.Loading -> copy(sortMenuState = update(sortMenuState))
    is LinkDetailsCommentsState.Error -> copy(sortMenuState = update(sortMenuState))
    is LinkDetailsCommentsState.Empty -> copy(sortMenuState = update(sortMenuState))
    is LinkDetailsCommentsState.Content -> copy(sortMenuState = update(sortMenuState))
}

private fun LinkDetailsCommentsState.toLoading(): LinkDetailsCommentsState = LinkDetailsCommentsState.Loading(
    sortMenuState = sortMenuState,
)

private fun LinkDetailsCommentsState.toError(): LinkDetailsCommentsState = LinkDetailsCommentsState.Error(
    sortMenuState = sortMenuState,
)

private fun LinkDetailsCommentsState.toLoaded(): LinkDetailsCommentsState = LinkDetailsCommentsState.Empty(
    sortMenuState = sortMenuState,
)

private fun Resources.toRelatedState(): LinkDetailsRelatedState = if (data.isEmpty()) {
    LinkDetailsRelatedState.Empty
} else {
    LinkDetailsRelatedState.Content(
        items = data
            .map { item -> item.toRelatedItemState() }
            .toImmutableList(),
    )
}

private fun LinkDetailsScreenState.syncDownvoteVisibility(): LinkDetailsScreenState = copy(
    downvoteMenuState = downvoteMenuState.copy(
        isVisible = isLoggedIn && link?.canVoteDown == true,
    ),
)

private fun LinkCommentItemState.applyVoteUp(isRemovingVote: Boolean): LinkCommentItemState = copy(
    voteState = if (isRemovingVote) {
        voteState.removeVoteUp()
    } else {
        voteState.increaseVoteUp()
    },
)

private fun LinkCommentItemState.applyVoteDown(isRemovingVote: Boolean): LinkCommentItemState = copy(
    voteState = if (isRemovingVote) {
        voteState.removeVoteDown()
    } else {
        voteState.increaseVoteDown()
    },
)

private fun LinkCommentItemState.applyFavourite(favourited: Boolean): LinkCommentItemState = copy(
    isFavourite = !favourited,
)

private fun ImmutableList<LinkCommentItemState>.patchEditedResource(
    resource: ResourceItem,
): ImmutableList<LinkCommentItemState> = this.map { comment ->
    comment.patchEditedResource(resource)
}.toImmutableList()

private fun LinkCommentItemState.patchEditedResource(
    resource: ResourceItem,
): LinkCommentItemState {
    if (this.id == resource.id) {
        return resource.toLinkCommentItemState(
            linkIdOverride = this.linkId,
            linkSlugOverride = this.linkSlug,
        ).copy(
            replies = this.replies.patchEditedResource(resource),
        )
    }

    return copy(
        replies = replies.patchEditedResource(resource),
    )
}

private fun ImmutableList<LinkCommentItemState>.applyVoteById(
    commentId: Int,
    isRemovingVote: Boolean,
    direction: LinkCommentVoteDirection,
): ImmutableList<LinkCommentItemState> = this.map { comment ->
    comment.applyVoteById(
        commentId = commentId,
        isRemovingVote = isRemovingVote,
        direction = direction,
    )
}.toImmutableList()

private fun LinkCommentItemState.applyVoteById(
    commentId: Int,
    isRemovingVote: Boolean,
    direction: LinkCommentVoteDirection,
): LinkCommentItemState {
    val updated = if (this.id == commentId) {
        when (direction) {
            LinkCommentVoteDirection.Up -> this.applyVoteUp(isRemovingVote)
            LinkCommentVoteDirection.Down -> this.applyVoteDown(isRemovingVote)
        }
    } else {
        this
    }

    return updated.copy(
        replies = updated.replies.applyVoteById(
            commentId = commentId,
            isRemovingVote = isRemovingVote,
            direction = direction,
        ),
    )
}

private fun ImmutableList<LinkCommentItemState>.applyFavouriteById(
    commentId: Int,
    favourited: Boolean,
): ImmutableList<LinkCommentItemState> = this.map { comment ->
    comment.applyFavouriteById(
        commentId = commentId,
        favourited = favourited,
    )
}.toImmutableList()

private fun LinkCommentItemState.applyFavouriteById(
    commentId: Int,
    favourited: Boolean,
): LinkCommentItemState {
    val updated = if (this.id == commentId) {
        this.applyFavourite(favourited)
    } else {
        this
    }

    return updated.copy(
        replies = updated.replies.applyFavouriteById(
            commentId = commentId,
            favourited = favourited,
        ),
    )
}

internal fun ImmutableList<LinkCommentItemState>.applyTwitterEmbedStateById(
    commentId: Int,
    embedKey: String,
    newState: TwitterEmbedState,
): ImmutableList<LinkCommentItemState> = this.map { comment ->
    comment.applyTwitterEmbedStateById(
        commentId = commentId,
        embedKey = embedKey,
        newState = newState,
    )
}.toImmutableList()

private fun LinkCommentItemState.applyTwitterEmbedStateById(
    commentId: Int,
    embedKey: String,
    newState: TwitterEmbedState,
): LinkCommentItemState {
    val updated = if (id == commentId) {
        copy(
            embedContentState = embedContentState.updateTwitterEmbedStateIfMatches(
                embedKey = embedKey,
                newState = newState,
            ),
        )
    } else {
        this
    }

    return updated.copy(
        replies = updated.replies.applyTwitterEmbedStateById(
            commentId = commentId,
            embedKey = embedKey,
            newState = newState,
        ),
    )
}

private fun ImmutableList<LinkCommentItemState>.containsCommentWithId(commentId: Int): Boolean =
    any { comment ->
        comment.id == commentId || comment.replies.containsCommentWithId(commentId)
    }

private enum class LinkCommentVoteDirection {
    Up,
    Down,
}

private enum class RelatedLinkVoteDirection {
    Up,
    Down,
}

private fun VoteReasonType.toVoteReason(): VoteReason = when (this) {
    VoteReasonType.Duplicate -> VoteReason.Duplicate
    VoteReasonType.Spam -> VoteReason.Spam
    VoteReasonType.Fake -> VoteReason.Fake
    VoteReasonType.Wrong -> VoteReason.Wrong
    VoteReasonType.Invalid -> VoteReason.Invalid
}
