package pl.masslany.podkop.features.hits

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface HitsActions :
    PaginationActions,
    ResourceItemActions,
    TopBarActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onArchiveClicked()

    fun onArchiveDismissed()

    fun onArchivePreviousYearClicked()

    fun onArchiveNextYearClicked()

    fun onArchiveMonthClicked(month: Int)

    fun onArchiveConfirmed()

    fun onRefresh()
}
