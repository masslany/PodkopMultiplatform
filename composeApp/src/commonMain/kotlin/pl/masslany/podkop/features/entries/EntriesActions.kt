package pl.masslany.podkop.features.entries

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface EntriesActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onHotSortSelected(sortType: DropdownMenuItemType)

    fun onHotSortExpandedChanged(expanded: Boolean)

    fun onHotSortDismissed()

    fun onRefresh(sortType: DropdownMenuItemType)

    fun onComposerTextChanged(content: TextFieldValue)

    fun onComposerAdultChanged(adult: Boolean)

    fun onComposerPhotoAttachClicked()

    fun onComposerPhotoRemoved()

    fun onComposerDismissed()

    fun onComposerSubmit()
}
