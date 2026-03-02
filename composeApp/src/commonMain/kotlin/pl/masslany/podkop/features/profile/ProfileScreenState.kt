package pl.masslany.podkop.features.profile

import pl.masslany.podkop.features.profile.models.ProfileContentState
import pl.masslany.podkop.features.profile.models.ProfileListContentState

data class ProfileScreenState(
    val isLoading: Boolean,
    val isResourcesLoading: Boolean,
    val isPaginating: Boolean,
    val isLoggedIn: Boolean,
    val content: ProfileContentState,
    val listContent: ProfileListContentState,
) {
    companion object Companion {
        val initial = ProfileScreenState(
            isLoading = true,
            isResourcesLoading = false,
            isPaginating = false,
            isLoggedIn = false,
            content = ProfileContentState.Empty,
            listContent = ProfileListContentState.Empty,
        )
    }
}
