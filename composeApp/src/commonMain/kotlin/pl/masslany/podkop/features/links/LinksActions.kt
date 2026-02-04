package pl.masslany.podkop.features.links

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.features.resources.ResourceItemActions

@Stable
interface LinksActions : ResourceItemActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onRefresh(sortType: DropdownMenuItemType)
}
