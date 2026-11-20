package pl.masslany.podkop.features.favorites

import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType

internal fun FavouritesSortType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    FavouritesSortType.Newest -> DropdownMenuItemType.Newest
    FavouritesSortType.Oldest -> DropdownMenuItemType.Oldest
}

internal fun DropdownMenuItemType.toFavouritesSortType(): FavouritesSortType = when (this) {
    DropdownMenuItemType.Newest -> FavouritesSortType.Newest
    DropdownMenuItemType.Oldest -> FavouritesSortType.Oldest
    else -> throw IllegalArgumentException("Attempt to convert $this to FavouritesSortType")
}

internal fun FavouritesResourceType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    FavouritesResourceType.All -> DropdownMenuItemType.Everything
    FavouritesResourceType.Link -> DropdownMenuItemType.Links
    FavouritesResourceType.Entry -> DropdownMenuItemType.Entries
    FavouritesResourceType.LinkComment -> DropdownMenuItemType.LinkComments
    FavouritesResourceType.EntryComment -> DropdownMenuItemType.EntryComments
}

internal fun DropdownMenuItemType.toFavouritesResourceType(): FavouritesResourceType = when (this) {
    DropdownMenuItemType.Everything -> FavouritesResourceType.All
    DropdownMenuItemType.Links -> FavouritesResourceType.Link
    DropdownMenuItemType.Entries -> FavouritesResourceType.Entry
    DropdownMenuItemType.LinkComments -> FavouritesResourceType.LinkComment
    DropdownMenuItemType.EntryComments -> FavouritesResourceType.EntryComment
    else -> throw IllegalArgumentException("Attempt to convert $this to FavouritesResourceType")
}
