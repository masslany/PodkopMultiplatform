package pl.masslany.podkop.features.favorites.preview

import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.favorites.FavoritesActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpFavoritesActions :
    FavoritesActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions,
    ResourceItemActions by NoOpResourceItemActions {
    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit

    override fun onSortExpandedChanged(expanded: Boolean) = Unit

    override fun onSortDismissed() = Unit

    override fun onTypeSelected(type: DropdownMenuItemType) = Unit

    override fun onTypeExpandedChanged(expanded: Boolean) = Unit

    override fun onTypeDismissed() = Unit

    override fun onRefresh() = Unit
}
