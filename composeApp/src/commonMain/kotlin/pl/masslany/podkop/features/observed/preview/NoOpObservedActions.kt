package pl.masslany.podkop.features.observed.preview

import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.observed.ObservedActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpObservedActions :
    ObservedActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions,
    ResourceItemActions by NoOpResourceItemActions {
    override fun onTypeSelected(type: DropdownMenuItemType) = Unit

    override fun onTypeExpandedChanged(expanded: Boolean) = Unit

    override fun onTypeDismissed() = Unit

    override fun onRefresh() = Unit
}
