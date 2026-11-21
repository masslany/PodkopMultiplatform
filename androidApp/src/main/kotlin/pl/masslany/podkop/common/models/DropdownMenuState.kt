package pl.masslany.podkop.common.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DropdownMenuState(
    val items: ImmutableList<DropdownMenuItemType>,
    val selected: DropdownMenuItemType,
    val expanded: Boolean,
) {
    companion object {
        val initial = DropdownMenuState(
            items = persistentListOf(),
            selected = DropdownMenuItemType.Newest,
            expanded = false,
        )
    }
}
