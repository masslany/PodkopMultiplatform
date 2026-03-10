package pl.masslany.podkop.features.profile

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface ProfileActions :
    TopBarActions,
    ResourceItemActions,
    PaginationActions {
    fun onRetryClicked()

    fun onDetailsToggleClicked()

    fun onNoteContentChanged(content: String)

    fun onNoteSaveClicked()

    fun onObserveClicked()

    fun onBlacklistClicked()

    fun onPrivateMessageClicked()

    fun onSummarySelected(type: ProfileSummaryType)

    fun onSubActionExpandedChanged(expanded: Boolean)

    fun onSubActionDismissed()

    fun onSubActionSelected(type: ProfileSubActionType)
}
