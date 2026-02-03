package pl.masslany.podkop.features.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.toDropdownMenuItemType
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

class EntriesViewModel(
    private val entriesRepository: EntriesRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
) : ViewModel(), EntriesActions, ResourceItemStateHolder by resourceItemStateHolder {

    private val _state = MutableStateFlow(EntriesScreenState.initial)
    val state = combine(
        _state,
        resourceItemStateHolder.items,
    ) { state, entries ->
        state.copy(
            entries = entries,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), EntriesScreenState.initial)

    init {
        resourceItemStateHolder.init(viewModelScope)

        _state.update { previousState ->
            previousState.copy(
                sortMenuState = DropdownMenuState(
                    items = entriesRepository.getEntriesSortTypes()
                        .map { entriesSortType -> entriesSortType.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = DropdownMenuItemType.Hot,
                    expanded = false
                )
            )
        }

        viewModelScope.launch {
            entriesRepository.getEntries(
                page = 1,
                limit = null,
                entriesSortType = EntriesSortType.Hot,
                hotSortType = HotSortType.TwelveHours,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    println("DBG --> failed to load links with $it")
                }
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateRefreshing(true)
        }

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

    override fun onRefresh(sortType: DropdownMenuItemType) {
        _state.update { previousState ->
            previousState
                .updateRefreshing(true)
        }

    }
}
