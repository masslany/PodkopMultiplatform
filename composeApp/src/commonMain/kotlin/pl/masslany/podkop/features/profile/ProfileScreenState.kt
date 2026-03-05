package pl.masslany.podkop.features.profile

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem
import pl.masslany.podkop.features.profile.models.ProfileSummaryType

data class ProfileScreenState(
    val username: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isResourcesLoading: Boolean,
    val isPaginating: Boolean,
    val header: ProfileHeaderState?,
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
            header = null,
            summary = persistentListOf(),
            selectedSummaryType = ProfileSummaryType.Actions,
            subActionState = ProfileSubActionState.initial,
            listContent = ProfileListContentState.Empty,
        )
    }
}
