package pl.masslany.podkop.features.entries

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.features.resources.ResourceItemActions

@Stable
interface EntriesActions : ResourceItemActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onHotSortSelected(sortType: DropdownMenuItemType)

    fun onHotSortExpandedChanged(expanded: Boolean)

    fun onHotSortDismissed()

    fun onRefresh(sortType: DropdownMenuItemType)
}
