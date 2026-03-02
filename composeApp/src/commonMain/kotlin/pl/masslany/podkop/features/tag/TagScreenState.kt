package pl.masslany.podkop.features.tag

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class TagScreenState(
    val screenInstanceId: String,
    val tag: String,
    val bannerUrl: String,
    val isGalleryMode: Boolean,
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isTagContentError: Boolean,
    val resources: ImmutableList<ResourceItemState>,
    val galleryItems: ImmutableList<TagGalleryItemState>,
    val sortMenuState: DropdownMenuState,
    val typeMenuState: DropdownMenuState,
    val isPaginating: Boolean,
    val isLoggedIn: Boolean,
) {
    companion object {
        val initial = TagScreenState(
            screenInstanceId = "",
            tag = "",
            bannerUrl = "",
            isGalleryMode = false,
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isTagContentError = false,
            resources = persistentListOf(),
            galleryItems = persistentListOf(),
            sortMenuState = DropdownMenuState.initial,
            typeMenuState = DropdownMenuState.initial,
            isPaginating = false,
            isLoggedIn = false,
        )

        private const val TAG_GALLERY_BASE_HEADER_ITEMS_COUNT = 3
    }

    val headerItemsCount: Int
        get() = TAG_GALLERY_BASE_HEADER_ITEMS_COUNT + if (isTagContentError) {
            1
        } else {
            0
        }

    fun findClosestGalleryIndex(
        resourceIndex: Int,
    ): Int? {
        if (galleryItems.isEmpty()) {
            return null
        }
        return galleryItems.indexOfFirst { it.resourceIndex >= resourceIndex }
            .takeIf { it >= 0 }
            ?: galleryItems.lastIndex
    }

    fun updateTag(tag: String) = this.copy(
        tag = tag,
    )

    fun updateBannerUrl(bannerUrl: String) = this.copy(
        bannerUrl = bannerUrl,
    )

    fun updateGalleryMode(isGalleryMode: Boolean) = this.copy(
        isGalleryMode = isGalleryMode,
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
