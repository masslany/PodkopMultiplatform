package pl.masslany.podkop.features.linkdetails

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface LinkDetailsActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onRefresh()

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onShowMoreRepliesClicked(commentId: Int, nextPage: Int)

    fun onComposerTextChanged(content: String)

    fun onComposerDismissed()

    fun onComposerSubmit()
}
