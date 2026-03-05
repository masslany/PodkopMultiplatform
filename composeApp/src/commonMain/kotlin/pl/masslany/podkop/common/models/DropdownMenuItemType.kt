package pl.masslany.podkop.common.models

import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
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

    data object Day : DropdownMenuItemType()

    data object Week : DropdownMenuItemType()

    data object Month : DropdownMenuItemType()

    data object Year : DropdownMenuItemType()

    data object Links : DropdownMenuItemType()

    data object Entries : DropdownMenuItemType()

    data object Everything : DropdownMenuItemType()
}

fun EntriesSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    EntriesSortType.Active -> DropdownMenuItemType.Active
    EntriesSortType.Hot -> DropdownMenuItemType.Hot
    EntriesSortType.Newest -> DropdownMenuItemType.Newest
}

fun LinksSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    LinksSortType.Active -> DropdownMenuItemType.Active
    LinksSortType.Commented -> DropdownMenuItemType.Commented
    LinksSortType.Digged -> DropdownMenuItemType.Digged
    LinksSortType.Newest -> DropdownMenuItemType.Newest
}

fun HotSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    HotSortType.TwoHours -> DropdownMenuItemType.TwoHours
    HotSortType.SixHours -> DropdownMenuItemType.SixHours
    HotSortType.TwelveHours -> DropdownMenuItemType.TwelveHours
}

fun DropdownMenuItemType.toLinksSortType(): LinksSortType = when (this) {
    DropdownMenuItemType.Active -> LinksSortType.Active
    DropdownMenuItemType.Commented -> LinksSortType.Commented
    DropdownMenuItemType.Digged -> LinksSortType.Digged
    DropdownMenuItemType.Newest -> LinksSortType.Newest
    else -> throw IllegalArgumentException("Attempt to convert $this to LinksSortType")
}

fun DropdownMenuItemType.toEntriesSortType(): EntriesSortType = when (this) {
    DropdownMenuItemType.Active -> EntriesSortType.Active
    DropdownMenuItemType.Hot -> EntriesSortType.Hot
    DropdownMenuItemType.Newest -> EntriesSortType.Newest
    else -> throw IllegalArgumentException("Attempt to convert $this to EntriesSortType")
}

fun DropdownMenuItemType.toHotSortType(): HotSortType = when (this) {
    DropdownMenuItemType.TwoHours -> HotSortType.TwoHours
    DropdownMenuItemType.SixHours -> HotSortType.SixHours
    DropdownMenuItemType.TwelveHours -> HotSortType.TwelveHours
    else -> throw IllegalArgumentException("Attempt to convert $this to HotSortType")
}
