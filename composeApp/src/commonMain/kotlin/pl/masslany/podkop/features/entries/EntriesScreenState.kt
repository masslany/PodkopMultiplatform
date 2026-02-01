package pl.masslany.podkop.features.entries

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class EntriesScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val entries: ImmutableList<ResourceItemState>,
    val sortMenuState: DropdownMenuState,
    val durationMenuState: DropdownMenuState,
) {
    companion object Companion {
        val initial = EntriesScreenState(
            isLoading = true,
            isRefreshing = false,
            entries = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            durationMenuState = DropdownMenuState.initial,
        )
    }

    fun updateSortMenuExpanded(expanded: Boolean) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = expanded
        )
    )

    fun updateSortMenuSelected(sortType: DropdownMenuItemType) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
            selected = sortType,
        )
    )

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )

}
