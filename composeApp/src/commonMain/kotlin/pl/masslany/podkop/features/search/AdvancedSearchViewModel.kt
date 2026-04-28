package pl.masslany.podkop.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.search.domain.main.SearchRepository
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.requireNumber
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

class AdvancedSearchViewModel(
    screen: AdvancedSearchScreen,
    private val searchRepository: SearchRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    AdvancedSearchActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentRequest: SearchStreamQuery? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated advanced search results", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        val activeRequest = currentRequest ?: return@Paginator Result.success(Resources(emptyList(), null))
        val currentItemCount = resourceItemStateHolder.items.value.size
        val page = request.requireNumber()
        searchRepository.getSearchStream(
            page = page,
            limit = null,
            query = activeRequest,
        ).map { resources ->
            resources.withSearchFallbackPagination(
                currentItemCount = currentItemCount,
                currentPage = page,
            )
        }
    }

    private val _state = MutableStateFlow(
        AdvancedSearchScreenState.initial.copy(query = screen.initialQuery),
    )
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, items, paginatorState ->
        state.copy(
            results = items,
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        _state.value,
    )

    init {
        resourceItemStateHolder.init(viewModelScope)
    }

    override fun onQueryChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                query = value,
                validationError = null,
            )
        }
    }

    override fun onSortSelected(sort: SearchSort) {
        _state.update { previous ->
            previous.copy(
                sort = sort,
                validationError = null,
            )
        }
    }

    override fun onMinimumVotesSelected(value: Int?) {
        _state.update { previous ->
            previous.copy(
                minimumVotes = value,
                validationError = null,
            )
        }
    }

    override fun onDatePresetSelected(preset: AdvancedSearchDatePreset) {
        _state.update { previous ->
            previous.copy(
                datePreset = preset,
                validationError = null,
            )
        }
    }

    override fun onCustomDateFromChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                customDateFrom = value,
                validationError = null,
            )
        }
    }

    override fun onCustomDateToChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                customDateTo = value,
                validationError = null,
            )
        }
    }

    override fun onTagsChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                tags = value,
                validationError = null,
            )
        }
    }

    override fun onUsersChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                users = value,
                validationError = null,
            )
        }
    }

    override fun onDomainsChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                domains = value,
                validationError = null,
            )
        }
    }

    override fun onCategoryChanged(value: String) {
        _state.update { previous ->
            previous.copy(
                category = value,
                validationError = null,
            )
        }
    }

    override fun onSearchClicked() {
        viewModelScope.launch {
            when (val result = state.value.toSearchRequest()) {
                is AdvancedSearchRequestResult.Error -> {
                    _state.update { previous ->
                        previous.copy(validationError = result.validationError)
                    }
                }

                is AdvancedSearchRequestResult.Success -> {
                    executeSearch(
                        request = result.request,
                        isRefresh = false,
                    )
                }
            }
        }
    }

    override fun onRefresh() {
        val request = currentRequest ?: return
        viewModelScope.launch {
            executeSearch(
                request = request,
                isRefresh = true,
            )
        }
    }

    fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = state.value.hasSearched &&
        state.value.results.isNotEmpty() &&
        !state.value.isLoading &&
        !state.value.isError &&
        paginator.shouldPaginate(lastVisibleIndex, totalItems)

    fun paginate() {
        if (currentRequest == null) {
            return
        }
        paginator.paginate()
    }

    private suspend fun executeSearch(
        request: SearchStreamQuery,
        isRefresh: Boolean,
    ) {
        currentRequest = request

        if (isRefresh) {
            _state.update { previous ->
                previous.copy(
                    hasSearched = true,
                    isRefreshing = true,
                    isError = false,
                    validationError = null,
                )
            }
        } else {
            resourceItemStateHolder.updateData(emptyList())
            _state.update { previous ->
                previous.copy(
                    hasSearched = true,
                    isLoading = true,
                    isRefreshing = false,
                    isError = false,
                    totalResults = null,
                    validationError = null,
                )
            }
        }

        searchRepository.getSearchStream(
            page = 1,
            limit = null,
            query = request,
        ).onSuccess { resources ->
            resourceItemStateHolder.updateData(resources.data)
            paginator.setup(resources.pagination, resources.data.size)
            _state.update { previous ->
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = false,
                    totalResults = resources.pagination?.total,
                )
            }
        }.onFailure { throwable ->
            logger.error("Failed to load advanced search results", throwable)
            _state.update { previous ->
                previous.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = true,
                    totalResults = null,
                )
            }
            snackbarManager.tryEmitGenericError()
        }
    }
}
