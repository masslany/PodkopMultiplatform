package pl.masslany.podkop.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.PaginationMode
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.pagination.FeaturePaginationPolicies
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class FavoritesViewModel(
    private val authRepository: AuthRepository,
    private val favouritesRepository: FavouritesRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    FavoritesActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentSortType: FavouritesSortType = FavouritesSortType.Newest
    private var currentResourceType: FavouritesResourceType = FavouritesResourceType.All
    private val screenInstanceId = Uuid.random().toString()

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated favourites", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        favouritesRepository.getFavourites(
            page = request,
            sortType = currentSortType,
            resourceType = currentResourceType,
        )
    }

    private val _state = MutableStateFlow(
        FavoritesScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, resources, paginatorState ->
        state.copy(
            resources = resources,
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        FavoritesScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

    init {
        resourceItemStateHolder.init(viewModelScope)
        _state.update { previousState ->
            previousState.copy(
                sortMenuState = DropdownMenuState(
                    items = favouritesRepository.getFavouritesSortTypes()
                        .map { it.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = currentSortType.toDropdownMenuItemType(),
                    expanded = false,
                ),
                typeMenuState = DropdownMenuState(
                    items = favouritesRepository.getFavouritesResourceTypes()
                        .map { it.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = currentResourceType.toDropdownMenuItemType(),
                    expanded = false,
                ),
            )
        }
        loadFavourites()
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        currentSortType = sortType.toFavouritesSortType()
        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateError(false)
                .updateRefreshing(true)
        }
        loadFavourites()
    }

    override fun onSortExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.updateSortMenuExpanded(expanded)
        }
    }

    override fun onSortDismissed() {
        _state.update { previousState ->
            previousState.updateSortMenuExpanded(false)
        }
    }

    override fun onTypeSelected(type: DropdownMenuItemType) {
        currentResourceType = type.toFavouritesResourceType()
        _state.update { previousState ->
            previousState
                .updateTypeMenuSelected(type)
                .updateError(false)
                .updateRefreshing(true)
        }
        loadFavourites()
    }

    override fun onTypeExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.updateTypeMenuExpanded(expanded)
        }
    }

    override fun onTypeDismissed() {
        _state.update { previousState ->
            previousState.updateTypeMenuExpanded(false)
        }
    }

    override fun onRefresh() {
        _state.update { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
        }
        loadFavourites()
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            val paginationMode = resolvePaginationMode()
            favouritesRepository.getFavourites(
                page = paginationMode.initialRequest(),
                sortType = currentSortType,
                resourceType = currentResourceType,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(
                        pagination = it.pagination,
                        initialItemCount = it.data.size,
                        paginationMode = paginationMode,
                    )
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error(
                        "Failed to load favourites for sort=$currentSortType type=$currentResourceType",
                        it,
                    )
                    val shouldShowErrorScreen = state.value.resources.isEmpty()
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(shouldShowErrorScreen)
                            .updateRefreshing(false)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private suspend fun resolvePaginationMode(): PaginationMode =
        FeaturePaginationPolicies.favourites(isLoggedIn = authRepository.isLoggedIn())
}
