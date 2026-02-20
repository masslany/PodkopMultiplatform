package pl.masslany.podkop.features.tag

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class TagScreenState(
    val tag: String,
    val bannerUrl: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isTagContentError: Boolean,
    val resources: ImmutableList<ResourceItemState>,
    val sortMenuState: DropdownMenuState,
    val typeMenuState: DropdownMenuState,
    val isPaginating: Boolean,
) {
    companion object {
        val initial = TagScreenState(
            tag = "",
            bannerUrl = "",
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isTagContentError = false,
            resources = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            typeMenuState = DropdownMenuState.initial,
            isPaginating = false,
        )
    }

    fun updateTag(tag: String) = this.copy(
        tag = tag,
    )

    fun updateBannerUrl(bannerUrl: String) = this.copy(
        bannerUrl = bannerUrl,
    )

    fun updateSortMenuExpanded(expanded: Boolean) = this.copy(
        sortMenuState = sortMenuState.copy(
            expanded = expanded,
        ),
        typeMenuState = typeMenuState.copy(
            expanded = false,
        ),
    )

    fun updateSortMenuSelected(sortType: DropdownMenuItemType) = this.copy(
        sortMenuState = sortMenuState.copy(
            selected = sortType,
            expanded = false,
        ),
        typeMenuState = typeMenuState.copy(
            expanded = false,
        ),
    )

    fun updateTypeMenuExpanded(expanded: Boolean) = this.copy(
        typeMenuState = typeMenuState.copy(
            expanded = expanded,
        ),
        sortMenuState = sortMenuState.copy(
            expanded = false,
        ),
    )

    fun updateTypeMenuSelected(type: DropdownMenuItemType) = this.copy(
        typeMenuState = typeMenuState.copy(
            selected = type,
            expanded = false,
        ),
        sortMenuState = sortMenuState.copy(
            expanded = false,
        ),
    )

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateError(isError: Boolean) = this.copy(
        isError = isError,
    )

    fun updateTagContentError(isTagContentError: Boolean) = this.copy(
        isTagContentError = isTagContentError,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}
