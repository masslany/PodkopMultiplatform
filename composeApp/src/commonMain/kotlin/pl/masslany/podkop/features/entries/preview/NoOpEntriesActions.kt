package pl.masslany.podkop.features.entries.preview

import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.entries.EntriesActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpEntriesActions :
    EntriesActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {

    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit
    override fun onSortExpandedChanged(expanded: Boolean) = Unit
    override fun onSortDismissed() = Unit
    override fun onHotSortSelected(sortType: DropdownMenuItemType) = Unit
    override fun onHotSortExpandedChanged(expanded: Boolean) = Unit
    override fun onHotSortDismissed() = Unit
    override fun onRefresh(sortType: DropdownMenuItemType) = Unit
    override fun onComposerTextChanged(content: TextFieldValue) = Unit
    override fun onComposerAdultChanged(adult: Boolean) = Unit
    override fun onComposerPhotoAttachClicked() = Unit
    override fun onComposerPhotoRemoved() = Unit
    override fun onComposerDismissed() = Unit
    override fun onComposerSubmit() = Unit
}
