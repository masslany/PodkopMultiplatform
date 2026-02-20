package pl.masslany.podkop.features.entrydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.toResourceItemState
import pl.masslany.podkop.features.topbar.TopBarActions

class EntryDetailsViewModel(
    private val id: Int,
    private val entriesRepository: EntriesRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntryDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated entry comments for id=$id", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        entriesRepository.getEntryComments(
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            entryId = id,
        )
    }

    private val _state = MutableStateFlow(EntryDetailsScreenState.initial)
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, comments, paginator ->
        logger.debug("Entry details comments updated: $comments")
        state.copy(
            comments = comments,
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), EntryDetailsScreenState.initial)

    init {
        resourceItemStateHolder.init(viewModelScope)
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    private fun loadContent(isRefreshing: Boolean) {
        _state.update { previousState ->
            previousState
                .updateLoading(!isRefreshing)
                .updateError(false)
                .updateCommentsError(false)
                .updateRefreshing(isRefreshing)
        }

        viewModelScope.launch {
            coroutineScope {
                val entryDeferred = async {
                    entriesRepository.getEntry(entryId = id)
                }
                val commentsDeferred = async {
                    entriesRepository.getEntryComments(
                        entryId = id,
                        page = 1,
                    )
                }

                val isEntryLoaded = entryDeferred.await()
                    .onSuccess {
                        _state.update { previousState ->
                            previousState.copy(entry = it.toResourceItemState())
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry details for id=$id", it)
                    }
                    .isSuccess

                commentsDeferred.await()
                    .onSuccess { comments ->
                        resourceItemStateHolder.updateData(comments.data)
                        paginator.setup(comments.pagination, comments.data.size)
                        _state.update { previousState ->
                            previousState.updateCommentsError(false)
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry comments for id=$id", it)
                        _state.update { previousState ->
                            previousState.updateCommentsError(true)
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                _state.update { previousState ->
                    previousState.updateError(!isEntryLoaded)
                }
            }

            _state.update { previousState ->
                previousState
                    .updateLoading(false)
                    .updateRefreshing(false)
            }
        }
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }
}
