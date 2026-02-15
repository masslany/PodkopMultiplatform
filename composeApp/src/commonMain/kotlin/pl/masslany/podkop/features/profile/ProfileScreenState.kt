package pl.masslany.podkop.features.profile

data class ProfileScreenState(
    val isLoading: Boolean,
    val content: ProfileContentState,
) {
    companion object Companion {
        val initial = ProfileScreenState(
            isLoading = true,
            content = ProfileContentState.Empty,
        )
    }
}

sealed interface ProfileContentState {
    data object Empty : ProfileContentState

    data object LoggedOut : ProfileContentState

    data object CurrentUser : ProfileContentState

    data class User(val username: String) : ProfileContentState
}
