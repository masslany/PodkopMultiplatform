package pl.masslany.podkop.features.hits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.requireNumber
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.hits.archivepicker.HitsArchivePickerState
import pl.masslany.podkop.features.hits.archivepicker.HitsArchiveState
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

class HitsViewModel(
    private val hitsRepository: HitsRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    HitsActions,
    TopBarActions by topBarActions,
    ResourceItemActions by resourceItemStateHolder {

    private val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private var selectedSortType: HitsSortType = HitsSortType.Day
    private var selectedArchive: HitsArchiveState? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated hits", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        hitsRepository.getLinkHits(
            page = request.requireNumber(),
            hitsSortType = selectedSortType,
            year = selectedArchive?.year,
            month = selectedArchive?.month,
        )
    }

    private val _state = MutableStateFlow(
        HitsScreenState.initial.copy(
            sortMenuState = DropdownMenuState(
                items = listOf(
                    HitsSortType.All,
                    HitsSortType.Day,
                    HitsSortType.Week,
                    HitsSortType.Month,
                    HitsSortType.Year,
                ).map { it.toDropdownMenuItemType() }.toImmutableList(),
                selected = selectedSortType.toDropdownMenuItemType(),
                expanded = false,
            ),
        ),
    )
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, items, paginatorState ->
        state.copy(
            resources = items,
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        _state.value,
    )

    init {
        resourceItemStateHolder.init(viewModelScope)
        fetchHits()
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        selectedSortType = sortType.toHitsSortType()
        selectedArchive = null

        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateSelectedArchive(null)
                .updateError(false)
                .updateRefreshing(true)
        }

        fetchHits()
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

    override fun onArchiveClicked() {
        _state.update { previousState ->
            previousState.updateArchivePickerState(
                HitsArchivePickerState.create(
                    selectedArchive = previousState.selectedArchive,
                    currentYear = currentDate.year,
                    currentMonth = currentDate.month.ordinal + 1,
                ),
            )
        }
    }

    override fun onArchiveDismissed() {
        _state.update { previousState ->
            previousState.updateArchivePickerState(null)
        }
    }

    override fun onArchivePreviousYearClicked() {
        _state.update { previousState ->
            previousState.updateArchivePickerState(
                previousState.archivePickerState?.selectPreviousYear(),
            )
        }
    }

    override fun onArchiveNextYearClicked() {
        _state.update { previousState ->
            previousState.updateArchivePickerState(
                previousState.archivePickerState?.selectNextYear(),
            )
        }
    }

    override fun onArchiveMonthClicked(month: Int) {
        _state.update { previousState ->
            previousState.updateArchivePickerState(
                previousState.archivePickerState?.selectMonth(month),
            )
        }
    }

    override fun onArchiveConfirmed() {
        val archivePickerState = state.value.archivePickerState ?: return
        selectedSortType = HitsSortType.All
        selectedArchive = archivePickerState.toArchiveState()

        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(DropdownMenuItemType.All)
                .updateSelectedArchive(selectedArchive)
                .updateArchivePickerState(null)
                .updateError(false)
                .updateRefreshing(true)
        }

        fetchHits()
    }

    override fun onRefresh() {
        _state.update { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
        }

        fetchHits()
    }

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean =
        paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }

    private fun fetchHits() {
        viewModelScope.launch {
            hitsRepository.getLinkHits(
                page = 1,
                hitsSortType = selectedSortType,
                year = selectedArchive?.year,
                month = selectedArchive?.month,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error(
                        message = "Failed to load hits for sort=${selectedSortType.value} archive=$selectedArchive",
                        throwable = it,
                    )
                    val shouldShowErrorScreen = state.value.resources.isEmpty()
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                            .updateError(shouldShowErrorScreen)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }
}
