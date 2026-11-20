package pl.masslany.podkop.features.favorites

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType
import pl.masslany.podkop.common.models.DropdownMenuItemType

class FavoritesSortMappersTest {

    @Test
    fun `sort mapper converts both directions`() {
        assertEquals(DropdownMenuItemType.Newest, FavouritesSortType.Newest.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.Oldest, FavouritesSortType.Oldest.toDropdownMenuItemType())
        assertEquals(FavouritesSortType.Newest, DropdownMenuItemType.Newest.toFavouritesSortType())
        assertEquals(FavouritesSortType.Oldest, DropdownMenuItemType.Oldest.toFavouritesSortType())
    }

    @Test
    fun `resource mapper converts both directions`() {
        assertEquals(DropdownMenuItemType.Everything, FavouritesResourceType.All.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.Links, FavouritesResourceType.Link.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.Entries, FavouritesResourceType.Entry.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.LinkComments, FavouritesResourceType.LinkComment.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.EntryComments, FavouritesResourceType.EntryComment.toDropdownMenuItemType())

        assertEquals(FavouritesResourceType.All, DropdownMenuItemType.Everything.toFavouritesResourceType())
        assertEquals(FavouritesResourceType.Link, DropdownMenuItemType.Links.toFavouritesResourceType())
        assertEquals(FavouritesResourceType.Entry, DropdownMenuItemType.Entries.toFavouritesResourceType())
        assertEquals(FavouritesResourceType.LinkComment, DropdownMenuItemType.LinkComments.toFavouritesResourceType())
        assertEquals(FavouritesResourceType.EntryComment, DropdownMenuItemType.EntryComments.toFavouritesResourceType())
    }
}
