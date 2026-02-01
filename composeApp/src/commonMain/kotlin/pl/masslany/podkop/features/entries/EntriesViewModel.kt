package pl.masslany.podkop.features.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

class EntriesViewModel(
    val resourceItemStateHolder: ResourceItemStateHolder,
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
                    items = persistentListOf(),
                    selected = DropdownMenuItemType.Hot,
                    expanded = false
                )
            )
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
