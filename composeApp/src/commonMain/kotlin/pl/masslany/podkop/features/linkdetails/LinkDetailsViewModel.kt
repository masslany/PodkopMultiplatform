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
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
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
    private val resourceItemStateHolder: ResourceItemStateHolder,
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

    private val _commentRepliesStateById = MutableStateFlow<Map<Int, CommentRepliesState>>(emptyMap())
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
        _commentRepliesStateById,
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

    override fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean) {
        resourceItemStateHolder.onLinkCommentVoteUpClick(
            linkId = linkId,
            commentId = commentId,
            voted = voted,
        )
        _commentRepliesStateById.update { previousState ->
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
        _commentRepliesStateById.update { previousState ->
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
                    .map { it.toLinkCommentItemState(linkIdOverride = id) }

                _commentRepliesStateById.update { previousState ->
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
                _commentRepliesStateById.update { previousState ->
                    val current = previousState[commentId] ?: return@update previousState
                    previousState + (commentId to current.copy(isLoadingReplies = false))
                }
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
                isRefreshing = isRefreshing,
                commentsState = previousState.commentsState.toLoading(),
                relatedState = LinkDetailsRelatedState.Loading,
            )
        }

        viewModelScope.launch {
            coroutineScope {
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

                linkDeferred.await()
                    .onSuccess { link ->
                        _state.update { previousState ->
                            previousState.copy(
                                link = link.data.toLinkItemState(isUpcoming = false),
                            )
                        }
                    }

                commentsDeferred.await().onSuccess { comments ->
                    onCommentsPageOneLoaded(comments)
                }.onFailure {
                    onCommentsPageOneLoadFailed()
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
                        _state.update { previousState ->
                            previousState.copy(
                                relatedState = LinkDetailsRelatedState.Error,
                            )
                        }
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
                onCommentsPageOneLoadFailed()
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
        _commentRepliesStateById.value = emptyMap()
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
        _commentRepliesStateById.value = buildInitialCommentRepliesState(items)
    }

    private fun appendCommentsRepliesState(items: List<ResourceItem>) {
        val newState = buildInitialCommentRepliesState(items)
        _commentRepliesStateById.update { previousState ->
            previousState + newState.filterKeys { key -> !previousState.containsKey(key) }
        }
    }

    private fun buildInitialCommentRepliesState(
        items: List<ResourceItem>,
    ): Map<Int, CommentRepliesState> = items.associate { item ->
        val replies = item.comments
            ?.items
            .orEmpty()
            .map { comment -> comment.toLinkCommentItemState(linkId = id) }
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
