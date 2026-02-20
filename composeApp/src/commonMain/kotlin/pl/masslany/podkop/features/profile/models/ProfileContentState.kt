package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList

sealed interface ProfileContentState {
    data object Empty : ProfileContentState

    data object LoggedOut : ProfileContentState

    data class Loaded(
        val isCurrentUser: Boolean,
        val header: ProfileHeaderState,
        val summary: ImmutableList<ProfileSummaryItem>,
        val selectedSummaryType: ProfileSummaryType,
        val subActionState: ProfileSubActionState,
    ) : ProfileContentState

    data object Error : ProfileContentState
}
