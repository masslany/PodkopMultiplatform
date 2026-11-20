package pl.masslany.podkop.features.favorites

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface FavoritesActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onTypeSelected(type: DropdownMenuItemType)

    fun onTypeExpandedChanged(expanded: Boolean)

    fun onTypeDismissed()

    fun onRefresh()
}
