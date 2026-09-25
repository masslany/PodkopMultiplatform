package pl.masslany.podkop.business.profile.domain.models

/** What the current viewer may do on a profile. Shared by Android and iOS presentation. */
data class ProfilePermissions(
    val isOwnProfile: Boolean,
    val canManageObservation: Boolean,
    val canBlacklist: Boolean,
    val canSendPrivateMessage: Boolean,
)

fun Profile.permissionsFor(
    isLoggedIn: Boolean,
    viewerUsername: String?,
): ProfilePermissions {
    val isOwnProfile = viewerUsername?.equals(name, ignoreCase = true) == true
    val observationEnabled = isLoggedIn && !isOwnProfile && canManageObservation
    return ProfilePermissions(
        isOwnProfile = isOwnProfile,
        canManageObservation = observationEnabled,
        canBlacklist = isLoggedIn && !isOwnProfile,
        canSendPrivateMessage = isLoggedIn && !isOwnProfile && (viewerUsername != null || observationEnabled),
    )
}
