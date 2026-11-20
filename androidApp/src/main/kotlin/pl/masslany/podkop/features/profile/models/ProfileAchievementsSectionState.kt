package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileAchievementsSectionState(
    val isLoading: Boolean,
    val isError: Boolean,
    val hasLoaded: Boolean,
    val items: ImmutableList<ProfileAchievementItemState>,
) {
    companion object Companion {
        val initial = ProfileAchievementsSectionState(
            isLoading = false,
            isError = false,
            hasLoaded = false,
            items = persistentListOf(),
        )
    }
}
