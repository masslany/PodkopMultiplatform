package pl.masslany.podkop.features.observed

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface ObservedActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onTypeSelected(type: DropdownMenuItemType)

    fun onTypeExpandedChanged(expanded: Boolean)

    fun onTypeDismissed()

    fun onRefresh()
}
