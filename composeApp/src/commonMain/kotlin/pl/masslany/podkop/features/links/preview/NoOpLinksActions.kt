package pl.masslany.podkop.features.links.preview

import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.links.LinksActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpLinksActions :
    LinksActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {

    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit
    override fun onSortExpandedChanged(expanded: Boolean) = Unit
    override fun onSortDismissed() = Unit
    override fun onRefresh(sortType: DropdownMenuItemType) = Unit
}
