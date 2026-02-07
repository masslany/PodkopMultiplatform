package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class LinksScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isUpcoming: Boolean,
    val links: ImmutableList<ResourceItemState>,
    val hits: ImmutableList<ResourceItemState>,
    val sortMenuState: DropdownMenuState,
    val isPaginating: Boolean,
) {
    companion object {
        val initial = LinksScreenState(
            isLoading = true,
            isRefreshing = false,
            isUpcoming = false,
            links = persistentListOf(),
            hits = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            isPaginating = false,
        )
    }

    fun updateSortMenuExpanded(expanded: Boolean) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = expanded,
        ),
    )

    fun updateSortMenuSelected(sortType: DropdownMenuItemType) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
            selected = sortType,
        ),
    )

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}
