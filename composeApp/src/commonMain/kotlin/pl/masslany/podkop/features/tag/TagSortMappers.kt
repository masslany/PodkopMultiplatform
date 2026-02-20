package pl.masslany.podkop.features.tag

import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType
import pl.masslany.podkop.common.models.DropdownMenuItemType

internal fun TagsSort.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    TagsSort.All -> DropdownMenuItemType.All
    TagsSort.Best -> DropdownMenuItemType.Best
}

internal fun DropdownMenuItemType.toTagsSort(): TagsSort = when (this) {
    DropdownMenuItemType.All -> TagsSort.All
    DropdownMenuItemType.Best -> TagsSort.Best
    else -> throw IllegalArgumentException("Attempt to convert $this to TagsSort")
}

internal fun TagsType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    TagsType.All -> DropdownMenuItemType.Everything
    TagsType.Links -> DropdownMenuItemType.Links
    TagsType.Entries -> DropdownMenuItemType.Entries
}

internal fun DropdownMenuItemType.toTagsType(): TagsType = when (this) {
    DropdownMenuItemType.Everything -> TagsType.All
    DropdownMenuItemType.All -> TagsType.All
    DropdownMenuItemType.Links -> TagsType.Links
    DropdownMenuItemType.Entries -> TagsType.Entries
    else -> throw IllegalArgumentException("Attempt to convert $this to TagsType")
}
