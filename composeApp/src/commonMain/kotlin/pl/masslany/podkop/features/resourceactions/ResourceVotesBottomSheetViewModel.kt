package pl.masslany.podkop.features.resourceactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.common.domain.models.common.Voter
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.UserItemState
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.vote.VoteReasonType
import pl.masslany.podkop.common.models.vote.toStringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.requireNumber
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.profile.ProfileScreen

class ResourceVotesBottomSheetViewModel(
    private val params: ResourceVotesParams,
    private val entriesRepository: EntriesRepository,
    private val linksRepository: LinksRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ResourceVotesBottomSheetActions {

    private val _state = MutableStateFlow(ResourceVotesBottomSheetState.initial)

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { newItems ->
            _state.update { previous ->
                previous.copy(
                    items = previous.items
                        .appendDistinctByUsername(newItems.map { it.toUserItemState() })
                        .toImmutableList(),
                )
            }
        },
        onError = { throwable ->
            logger.error("Failed to paginate resource vote users for $params", throwable)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        val page = request.requireNumber()

        loadVotes(page = page)
    }

    val state = combine(
        _state,
        paginator.state,
    ) { state, paginatorState ->
        state.copy(
            isPaginating = paginatorState is PaginatorState.Loading && !state.isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = ResourceVotesBottomSheetState.initial,
    )

    init {
        onRetryClicked()
    }

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean {
        val state = _state.value
        if (state.isLoading || state.isError) return false
        return paginator.shouldPaginate(lastVisibleIndex, totalItems)
    }

    override fun paginate() {
        val state = _state.value
        if (state.isLoading || state.isError) return
        paginator.paginate()
    }

    override fun onUserClicked(username: String) {
        appNavigator.back()
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onRetryClicked() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    items = emptyList<UserItemState>().toImmutableList(),
                )
            }
            paginator.setup(pagination = null, initialItemCount = 0)

            loadVotes(page = 1)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isError = false,
                            items = response.data
                                .map { voter -> voter.toUserItemState() }
                                .distinctBy { user -> user.username }
                                .toImmutableList(),
                        )
                    }
                    paginator.setup(response.pagination, response.data.size)
                }
                .onFailure { throwable ->
                    logger.error("Failed to load resource vote users for $params", throwable)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            items = emptyList<UserItemState>().toImmutableList(),
                        )
                    }
                }
        }
    }

    private suspend fun loadVotes(page: Int) = when (params.resourceType) {
        ResourceVotesType.Entry -> entriesRepository.getEntryVotes(
            entryId = params.entryId,
            page = page,
        )

        ResourceVotesType.EntryComment -> entriesRepository.getEntryCommentVotes(
            entryId = params.entryId,
            commentId = requireNotNull(params.entryCommentId) { "Entry comment votes require entryCommentId" },
            page = page,
        )

        ResourceVotesType.LinkUp -> linksRepository.getLinkUpvotes(
            linkId = params.linkId,
            type = "up",
            page = page,
        )

        ResourceVotesType.LinkDown -> linksRepository.getLinkUpvotes(
            linkId = params.linkId,
            type = "down",
            page = page,
        )
    }
}

private fun Voter.toUserItemState(): UserItemState = UserItemState(
    username = username,
    avatarUrl = avatar,
    genderIndicatorType = gender.toGenderIndicatorType(),
    nameColorType = color.toNameColorType(),
    online = online,
    company = company,
    verified = verified,
    status = status,
    voteReason = reason?.toVoteReasonType()?.toStringResource(),
)

private fun VoteReason.toVoteReasonType(): VoteReasonType = when (this) {
    VoteReason.Duplicate -> VoteReasonType.Duplicate
    VoteReason.Spam -> VoteReasonType.Spam
    VoteReason.Fake -> VoteReasonType.Fake
    VoteReason.Wrong -> VoteReasonType.Wrong
    VoteReason.Invalid -> VoteReasonType.Invalid
}

private fun List<UserItemState>.appendDistinctByUsername(
    incoming: List<UserItemState>,
): List<UserItemState> {
    val known = mapTo(mutableSetOf()) { it.username }
    val newItems = incoming.filter { known.add(it.username) }
    return this + newItems
}
