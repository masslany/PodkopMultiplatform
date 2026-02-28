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
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.link.toLinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.toLinkCommentItemState
import pl.masslany.podkop.features.resources.models.related.toRelatedItemState
import pl.masslany.podkop.features.topbar.TopBarActions

class LinkDetailsViewModel(
    private val id: Int,
    private val linksRepository: LinksRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val appNavigator: AppNavigator,
    private val screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
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
    private val _state = MutableStateFlow(
        LinkDetailsScreenState.initial.copy(
            commentsState = LinkDetailsCommentsState.Loading(
                sortMenuState = DropdownMenuState(
                    items = availableCommentsSortTypes
                        .map { it.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = selectedCommentsSortType.toDropdownMenuItemType(),
                    expanded = false,
                ),
            ),
        ),
    )
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
        commentRepliesStateById,
    ) { state, comments, paginatorState, repliesStateById ->
        val mappedComments = comments
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
            commentsState = state.commentsState.resolve(
                comments = mappedComments,
                isPaginating = paginatorState is PaginatorState.Loading,
            ),
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), _state.value)

    init {
        resourceItemStateHolder.init(viewModelScope)
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    override fun onComposerTextChanged(content: String) {
        _state.update { previousState ->
            previousState.copy(composerContent = content)
        }
    }

    override fun onComposerDismissed() {
        _state.update { previousState ->
            previousState.copy(
                isComposerVisible = false,
                composerContent = "",
                composerReplyTarget = null,
                composerParentCommentId = null,
                isComposerSubmitting = false,
            )
        }
    }

    override fun onComposerSubmit() {
        val currentState = state.value
        if (!currentState.isLoggedIn || !currentState.isComposerVisible || currentState.isComposerSubmitting) {
            return
        }

        val content = currentState.composerContent.trim()
        if (content.isBlank()) {
            return
        }

        _state.update { previousState ->
            previousState.copy(isComposerSubmitting = true)
        }

        viewModelScope.launch {
            val submitResult = if (currentState.composerParentCommentId == null) {
                linksRepository.createLinkComment(
                    linkId = id,
                    content = content,
                    adult = false,
                )
            } else {
                linksRepository.createLinkCommentReply(
                    linkId = id,
                    commentId = currentState.composerParentCommentId,
                    content = content,
                    adult = false,
                )
            }

            submitResult.onSuccess { createdComment ->
                appendCreatedComment(
                    createdComment = createdComment,
                    repliedCommentId = currentState.composerParentCommentId,
                )
                _state.update { previousState ->
                    previousState.copy(
                        isComposerVisible = false,
                        composerContent = "",
                        composerReplyTarget = null,
                        composerParentCommentId = null,
                        isComposerSubmitting = false,
                    )
                }
            }.onFailure {
                logger.error("Failed to create link comment for id=$id", it)
                _state.update { previousState ->
                    previousState.copy(isComposerSubmitting = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onLinkReplyClicked(linkId: Int, author: String?) {
        if (linkId != id) return
        showComposerForAuthor(
            author = author,
            parentCommentId = null,
        )
    }

    override fun onLinkCommentReplyClicked(linkId: Int, commentId: Int, author: String?) {
        if (linkId != id) return
        val replyParentCommentId = resolveReplyParentCommentId(commentId) ?: return
        showComposerForAuthor(
            author = author,
            parentCommentId = replyParentCommentId,
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
                        voted = voted,
                    ),
                )
            }
        }
    }

    override fun onLinkCommentMoreClicked(
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    ) {
        val draftId = buildLinkCommentScreenshotDraftId(commentId)
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forLinkComment(
                linkId = linkId,
                linkSlug = linkSlug,
                linkCommentId = commentId,
                parentCommentId = parentCommentId,
                screenshotDraftId = draftId,
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
        _state.update { previousState ->
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
        _state.update { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.updateSortMenuState { menu ->
                    menu.copy(expanded = expanded)
                },
            )
        }
    }

    override fun onSortDismissed() {
        _state.update { previousState ->
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
        viewModelScope.launch {
            val voteResult = if (voted) {
                linksRepository.removeVoteOnLink(linkId = id)
            } else {
                linksRepository.voteOnLink(linkId = id)
            }

            voteResult.onSuccess {
                refreshLink(id)
            }
        }
    }

    private fun loadContent(isRefreshing: Boolean) {
        _state.update { previousState ->
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
                        _state.update { previousState ->
                            previousState.copy(
                                link = link.data.toLinkItemState(isUpcoming = false),
                            )
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load link details for id=$id", it)
                    }
                    .isSuccess

                viewerContextDeferred.await().let { viewerContext ->
                    _state.update { previousState ->
                        previousState.copy(
                            isLoggedIn = viewerContext.isLoggedIn,
                            currentUsername = viewerContext.username,
                            isComposerVisible = if (viewerContext.isLoggedIn) {
                                previousState.isComposerVisible
                            } else {
                                false
                            },
                            composerContent = if (viewerContext.isLoggedIn) {
                                previousState.composerContent
                            } else {
                                ""
                            },
                            composerReplyTarget = if (viewerContext.isLoggedIn) {
                                previousState.composerReplyTarget
                            } else {
                                null
                            },
                            composerParentCommentId = if (viewerContext.isLoggedIn) {
                                previousState.composerParentCommentId
                            } else {
                                null
                            },
                            isComposerSubmitting = if (viewerContext.isLoggedIn) {
                                previousState.isComposerSubmitting
                            } else {
                                false
                            },
                        )
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
                        _state.update { previousState ->
                            previousState.copy(
                                relatedState = if (related.data.isEmpty()) {
                                    LinkDetailsRelatedState.Empty
                                } else {
                                    LinkDetailsRelatedState.Content(
                                        items = related.data
                                            .map { item -> item.toRelatedItemState() }
                                            .toImmutableList(),
                                    )
                                },
                            )
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load related links for link id=$id", it)
                        _state.update { previousState ->
                            previousState.copy(
                                relatedState = LinkDetailsRelatedState.Error,
                            )
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                _state.update { previousState ->
                    previousState.copy(
                        isError = !isLinkLoaded,
                    )
                }
            }

            _state.update { previousState ->
                previousState.copy(
                    isLoading = false,
                    isRefreshing = false,
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
        resourceItemStateHolder.updateData(comments.data)
        replaceCommentsRepliesState(comments.data)
        paginator.setup(
            pagination = comments.pagination,
            initialItemCount = comments.data.size,
        )
        _state.update { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.toLoaded(),
            )
        }
    }

    private suspend fun onCommentsPageOneLoadFailed() {
        resourceItemStateHolder.updateData(emptyList())
        commentRepliesStateById.value = emptyMap()
        _state.update { previousState ->
            previousState.copy(
                commentsState = previousState.commentsState.toError(),
            )
        }
    }

    private suspend fun refreshLink(linkId: Int) {
        linksRepository.getLink(linkId)
            .onSuccess { link ->
                val updatedState = link.data.toLinkItemState(isUpcoming = false)
                val updatedRelatedState = link.data.toRelatedItemState()
                _state.update { previousState ->
                    previousState.copy(
                        link = if (previousState.link?.id == linkId) {
                            updatedState
                        } else {
                            previousState.link
                        },
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
                    )
                }
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

    private fun showComposerForAuthor(
        author: String?,
        parentCommentId: Int?,
    ) {
        if (!state.value.isLoggedIn) {
            return
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefill = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        _state.update { previousState ->
            previousState.copy(
                isComposerVisible = true,
                composerReplyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                composerContent = prefill,
                composerParentCommentId = parentCommentId,
                isComposerSubmitting = false,
            )
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

private fun LinkCommentItemState.applyVoteUp(voted: Boolean): LinkCommentItemState = copy(
    voteState = if (voted) {
        voteState.removeVoteUp()
    } else {
        voteState.increaseVoteUp()
    },
)

private fun ImmutableList<LinkCommentItemState>.applyVoteById(
    commentId: Int,
    voted: Boolean,
): ImmutableList<LinkCommentItemState> = this.map { comment ->
    comment.applyVoteById(commentId = commentId, voted = voted)
}.toImmutableList()

private fun LinkCommentItemState.applyVoteById(commentId: Int, voted: Boolean): LinkCommentItemState {
    val updated = if (this.id == commentId) {
        this.applyVoteUp(voted)
    } else {
        this
    }

    return updated.copy(
        replies = updated.replies.applyVoteById(
            commentId = commentId,
            voted = voted,
        ),
    )
}
