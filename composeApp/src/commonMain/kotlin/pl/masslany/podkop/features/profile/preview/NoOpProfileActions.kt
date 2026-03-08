package pl.masslany.podkop.features.profile.preview

import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.profile.ProfileActions
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpProfileActions :
    ProfileActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {

    override fun onRetryClicked() = Unit
    override fun onObserveClicked() = Unit
    override fun onPrivateMessageClicked() = Unit
    override fun onSummarySelected(type: ProfileSummaryType) = Unit
    override fun onSubActionExpandedChanged(expanded: Boolean) = Unit
    override fun onSubActionDismissed() = Unit
    override fun onSubActionSelected(type: ProfileSubActionType) = Unit
}
