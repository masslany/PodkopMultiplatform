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
    val hotSortMenuState: DropdownMenuState?,
) {
    companion object Companion {
        val initial = EntriesScreenState(
            isLoading = true,
            isRefreshing = false,
            entries = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            hotSortMenuState = null,
        )
    }

    fun updateSortMenuExpanded(expanded: Boolean) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = expanded,
        ),
        hotSortMenuState = hotSortMenuState?.copy(
            expanded = false,
        ),
    )

    fun updateSortMenuSelected(
        sortType: DropdownMenuItemType,
        hotSortTypes: ImmutableList<DropdownMenuItemType>,
    ) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
            selected = sortType,
        ),
        hotSortMenuState = if (sortType == DropdownMenuItemType.Hot) {
            DropdownMenuState(
                items = hotSortTypes,
                selected = DropdownMenuItemType.TwelveHours,
                expanded = false,
            )
        } else {
            null
        },
    )

    fun updateHotSortMenuExpanded(expanded: Boolean) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
        ),
        hotSortMenuState = hotSortMenuState?.copy(
            expanded = expanded,
        ),
    )

    fun updateHotSortMenuSelected(sortType: DropdownMenuItemType) = this.copy(
        hotSortMenuState = hotSortMenuState?.copy(
            selected = sortType,
            expanded = false,
        ),
    )

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}
