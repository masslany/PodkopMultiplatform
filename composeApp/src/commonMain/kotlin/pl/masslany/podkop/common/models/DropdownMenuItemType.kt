package pl.masslany.podkop.common.models

import pl.masslany.podkop.business.links.domain.models.request.LinksSortType

sealed class DropdownMenuItemType {
    data object Active : DropdownMenuItemType()

    data object Newest : DropdownMenuItemType()

    data object Commented : DropdownMenuItemType()

    data object Digged : DropdownMenuItemType()

    data object Best : DropdownMenuItemType()

    data object Oldest : DropdownMenuItemType()

    data object Hot : DropdownMenuItemType()

    data object TwoHours : DropdownMenuItemType()

    data object SixHours : DropdownMenuItemType()

    data object TwelveHours : DropdownMenuItemType()

    data object All : DropdownMenuItemType()

    data object Links : DropdownMenuItemType()

    data object Entries : DropdownMenuItemType()

    data object Everything : DropdownMenuItemType()
}

fun LinksSortType.toDropdownMenuItemType(): DropdownMenuItemType {
    return when (this) {
        LinksSortType.Active -> DropdownMenuItemType.Active
        LinksSortType.Commented -> DropdownMenuItemType.Commented
        LinksSortType.Digged -> DropdownMenuItemType.Digged
        LinksSortType.Newest -> DropdownMenuItemType.Newest
    }
}

fun DropdownMenuItemType.toLinksSortType(): LinksSortType {
    return when (this) {
         DropdownMenuItemType.Active -> LinksSortType.Active
         DropdownMenuItemType.Commented -> LinksSortType.Commented
         DropdownMenuItemType.Digged -> LinksSortType.Digged
         DropdownMenuItemType.Newest -> LinksSortType.Newest
        else -> throw IllegalArgumentException("Attempt to convert $this to LinksSortType")
    }
}