package pl.masslany.podkop.features.tag.preview

import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.tag.TagActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpTagActions :
    TagActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {

    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit
    override fun onSortExpandedChanged(expanded: Boolean) = Unit
    override fun onSortDismissed() = Unit
    override fun onTypeSelected(type: DropdownMenuItemType) = Unit
    override fun onTypeExpandedChanged(expanded: Boolean) = Unit
    override fun onTypeDismissed() = Unit
    override fun onGalleryModeToggled() = Unit
    override fun onRefresh() = Unit
    override fun onObserveClicked() = Unit
    override fun onNotificationsClicked() = Unit
}
