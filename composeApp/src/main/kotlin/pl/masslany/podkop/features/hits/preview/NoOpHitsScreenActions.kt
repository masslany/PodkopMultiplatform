package pl.masslany.podkop.features.hits.preview

import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.hits.HitsActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpHitsScreenActions :
    HitsActions,
    ResourceItemActions by NoOpResourceItemActions,
    TopBarActions by NoOpTopBarActions {

    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit

    override fun onSortExpandedChanged(expanded: Boolean) = Unit

    override fun onSortDismissed() = Unit

    override fun onArchiveClicked() = Unit

    override fun onArchiveDismissed() = Unit

    override fun onArchivePreviousYearClicked() = Unit

    override fun onArchiveNextYearClicked() = Unit

    override fun onArchiveMonthClicked(month: Int) = Unit

    override fun onArchiveConfirmed() = Unit

    override fun onRefresh() = Unit

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean = false

    override fun paginate() = Unit
}
