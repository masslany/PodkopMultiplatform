package pl.masslany.podkop.features.profile

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.profile.models.ProfileAchievementsSectionState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileNoteState
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem
import pl.masslany.podkop.features.profile.models.ProfileSummaryType

data class ProfileScreenState(
    val username: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isResourcesLoading: Boolean,
    val isPaginating: Boolean,
    val isObserveActionLoading: Boolean,
    val isBlacklistActionLoading: Boolean,
    val isDetailsExpanded: Boolean,
    val header: ProfileHeaderState?,
    val noteState: ProfileNoteState,
    val achievementsState: ProfileAchievementsSectionState,
    val summary: ImmutableList<ProfileSummaryItem>,
    val selectedSummaryType: ProfileSummaryType,
    val subActionState: ProfileSubActionState,
    val listContent: ProfileListContentState,
) {
    companion object Companion {
        val initial = ProfileScreenState(
            username = "",
            isLoading = true,
            isError = false,
            isResourcesLoading = false,
            isPaginating = false,
            isObserveActionLoading = false,
            isBlacklistActionLoading = false,
            isDetailsExpanded = false,
            header = null,
            noteState = ProfileNoteState.initial,
            achievementsState = ProfileAchievementsSectionState.initial,
            summary = persistentListOf(),
            selectedSummaryType = ProfileSummaryType.Actions,
            subActionState = ProfileSubActionState.initial,
            listContent = ProfileListContentState.Empty,
        )
    }
}
