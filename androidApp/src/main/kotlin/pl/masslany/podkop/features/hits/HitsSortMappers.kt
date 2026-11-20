package pl.masslany.podkop.features.hits

import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType

internal fun HitsSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    HitsSortType.All -> DropdownMenuItemType.All
    HitsSortType.Day -> DropdownMenuItemType.Day
    HitsSortType.Week -> DropdownMenuItemType.Week
    HitsSortType.Month -> DropdownMenuItemType.Month
    HitsSortType.Year -> DropdownMenuItemType.Year
}

internal fun DropdownMenuItemType.toHitsSortType(): HitsSortType = when (this) {
    DropdownMenuItemType.All -> HitsSortType.All
    DropdownMenuItemType.Day -> HitsSortType.Day
    DropdownMenuItemType.Week -> HitsSortType.Week
    DropdownMenuItemType.Month -> HitsSortType.Month
    DropdownMenuItemType.Year -> HitsSortType.Year
    else -> throw IllegalArgumentException("Attempt to convert $this to HitsSortType")
}
