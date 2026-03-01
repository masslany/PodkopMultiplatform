package pl.masslany.podkop.features.linkdetails.preview

import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.linkdetails.LinkDetailsActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpLinkDetailsActions :
    LinkDetailsActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {

    override fun onRefresh() = Unit
    override fun onSortSelected(sortType: DropdownMenuItemType) = Unit
    override fun onSortExpandedChanged(expanded: Boolean) = Unit
    override fun onSortDismissed() = Unit
    override fun onShowMoreRepliesClicked(commentId: Int, nextPage: Int) = Unit
    override fun onComposerTextChanged(content: TextFieldValue) = Unit
    override fun onComposerAdultChanged(adult: Boolean) = Unit
    override fun onComposerDismissed() = Unit
    override fun onComposerSubmit() = Unit
}
