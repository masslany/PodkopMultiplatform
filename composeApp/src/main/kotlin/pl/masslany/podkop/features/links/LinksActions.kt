package pl.masslany.podkop.features.links

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface LinksActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onRefresh(sortType: DropdownMenuItemType)

    fun onRefreshPromptDismissed()
}
