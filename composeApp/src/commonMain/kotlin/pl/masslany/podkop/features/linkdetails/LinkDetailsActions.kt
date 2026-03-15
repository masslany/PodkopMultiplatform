package pl.masslany.podkop.features.linkdetails

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.vote.VoteReasonType
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface LinkDetailsActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onRefresh()

    fun onLinkDownvoteClicked(linkId: Int, isDownVoted: Boolean)

    fun onLinkDownvoteReasonSelected(linkId: Int, reason: VoteReasonType)

    fun onLinkDownvoteDismissed()

    fun onSortSelected(sortType: DropdownMenuItemType)

    fun onSortExpandedChanged(expanded: Boolean)

    fun onSortDismissed()

    fun onShowMoreRepliesClicked(commentId: Int, nextPage: Int)

    fun onRelatedVoteUpClicked(relatedId: Int, voted: Boolean)

    fun onRelatedVoteDownClicked(relatedId: Int, voted: Boolean)
}
