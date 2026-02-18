package pl.masslany.podkop.features.linkdetails

import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType

internal fun CommentsSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    CommentsSortType.Best -> DropdownMenuItemType.Best
    CommentsSortType.Newest -> DropdownMenuItemType.Newest
    CommentsSortType.Oldest -> DropdownMenuItemType.Oldest
}

internal fun DropdownMenuItemType.toCommentsSortType(): CommentsSortType = when (this) {
    DropdownMenuItemType.Best -> CommentsSortType.Best
    DropdownMenuItemType.Newest -> CommentsSortType.Newest
    DropdownMenuItemType.Oldest -> CommentsSortType.Oldest
    else -> throw IllegalArgumentException("Attempt to convert $this to CommentsSortType")
}
