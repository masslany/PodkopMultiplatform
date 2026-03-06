package pl.masslany.podkop.features.favorites

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class FavoritesScreenState(
    val screenInstanceId: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val resources: ImmutableList<ResourceItemState>,
    val sortMenuState: DropdownMenuState,
    val typeMenuState: DropdownMenuState,
    val isPaginating: Boolean,
) {
    companion object {
        val initial = FavoritesScreenState(
            screenInstanceId = "",
            isLoading = true,
            isError = false,
            isRefreshing = false,
            resources = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            typeMenuState = DropdownMenuState.initial,
            isPaginating = false,
        )
    }

    fun updateSortMenuExpanded(expanded: Boolean) = copy(
        sortMenuState = sortMenuState.copy(expanded = expanded),
        typeMenuState = typeMenuState.copy(expanded = false),
    )

    fun updateSortMenuSelected(sortType: DropdownMenuItemType) = copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
            selected = sortType,
        ),
    )

    fun updateTypeMenuExpanded(expanded: Boolean) = copy(
        sortMenuState = sortMenuState.copy(expanded = false),
        typeMenuState = typeMenuState.copy(expanded = expanded),
    )

    fun updateTypeMenuSelected(type: DropdownMenuItemType) = copy(
        typeMenuState = typeMenuState.copy(
            expanded = false,
            selected = type,
        ),
    )

    fun updateLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun updateError(isError: Boolean) = copy(isError = isError)

    fun updateRefreshing(isRefreshing: Boolean) = copy(isRefreshing = isRefreshing)
}
