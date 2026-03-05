package pl.masslany.podkop.features.hits

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.hits.archivepicker.HitsArchivePickerState
import pl.masslany.podkop.features.hits.archivepicker.HitsArchiveState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class HitsScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isPaginating: Boolean,
    val resources: ImmutableList<ResourceItemState>,
    val sortMenuState: DropdownMenuState,
    val selectedArchive: HitsArchiveState?,
    val archivePickerState: HitsArchivePickerState?,
) {
    companion object {
        val initial = HitsScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isPaginating = false,
            resources = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            selectedArchive = null,
            archivePickerState = null,
        )
    }

    fun updateSortMenuExpanded(expanded: Boolean) = copy(
        sortMenuState = sortMenuState.copy(expanded = expanded),
    )

    fun updateSortMenuSelected(sortType: DropdownMenuItemType) = copy(
        sortMenuState = sortMenuState.copy(
            expanded = false,
            selected = sortType,
        ),
    )

    fun updateLoading(isLoading: Boolean) = copy(
        isLoading = isLoading,
    )

    fun updateError(isError: Boolean) = copy(
        isError = isError,
    )

    fun updateRefreshing(isRefreshing: Boolean) = copy(
        isRefreshing = isRefreshing,
    )

    fun updateSelectedArchive(selectedArchive: HitsArchiveState?) = copy(
        selectedArchive = selectedArchive,
    )

    fun updateArchivePickerState(archivePickerState: HitsArchivePickerState?) = copy(
        archivePickerState = archivePickerState,
    )
}
