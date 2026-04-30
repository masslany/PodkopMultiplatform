package pl.masslany.podkop.features.observed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.observed.domain.main.ObservedRepository
import pl.masslany.podkop.business.observed.domain.models.ObservedResource
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.PaginationMode
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class ObservedViewModel(
    private val observedRepository: ObservedRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    ObservedActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentType: ObservedType = ObservedType.All
    private val screenInstanceId = Uuid.random().toString()
    private val observedItems = MutableStateFlow(emptyList<ObservedResource>().toImmutableList())

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            observedItems.update { previousItems ->
                (previousItems + data).toImmutableList()
            }
            resourceItemStateHolder.appendData(data.map { it.item })
        },
        onError = {
            logger.error("Failed to load paginated observed items", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        observedRepository.getObserved(
            page = request,
            type = currentType,
        )
    }

    private val observedTypes = ObservedType.entries
        .map { type -> type.toDropdownMenuItemType() }
        .toImmutableList()

    private val _state = MutableStateFlow(
        ObservedScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )
    val state = combine(
        _state,
        observedItems,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, observedItems, resources, paginatorState ->
        state.copy(
            items = resources.mapIndexed { index, resource ->
                observedItems.getOrNull(index)
                    ?.toObservedListItemState(resourceState = resource)
                    ?: resource.toObservedListItemState()
            }.toPersistentList(),
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        ObservedScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

    init {
        resourceItemStateHolder.init(viewModelScope)
        _state.update { previousState ->
            previousState.copy(
                selectedType = currentType,
                typeMenuState = DropdownMenuState(
                    items = observedTypes,
                    selected = currentType.toDropdownMenuItemType(),
                    expanded = false,
                ),
            )
        }
        loadObserved(
            showErrorScreenOnFailure = true,
        )
    }

    override fun onTypeSelected(type: DropdownMenuItemType) {
        currentType = type.toObservedType()
        _state.update { previousState ->
            previousState
                .updateTypeMenuSelected(type = type, observedType = currentType)
                .updateError(false)
                .updateRefreshing(true)
        }
        loadObserved(
            showErrorScreenOnFailure = state.value.items.isEmpty(),
        )
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
        loadObserved(
            showErrorScreenOnFailure = state.value.items.isEmpty(),
        )
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }

    private fun loadObserved(
        showErrorScreenOnFailure: Boolean,
    ) {
        viewModelScope.launch {
            observedRepository.getObserved(
                page = PaginationMode.CursorInPage.initialRequest(),
                type = currentType,
            )
                .onSuccess {
                    observedItems.value = it.data.toImmutableList()
                    resourceItemStateHolder.updateData(it.data.map { observedItem -> observedItem.item })
                    paginator.setup(it.pagination, it.data.size, PaginationMode.CursorInPage)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load observed content for type=$currentType", it)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(showErrorScreenOnFailure)
                            .updateRefreshing(false)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }
}
