package pl.masslany.podkop.features.profile.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType

data class ProfileHeaderState(
    val username: String,
    val avatarUrl: String,
    val backgroundUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
    val memberSinceState: MemberSinceState,
    val isLoggedIn: Boolean,
    val isOwnProfile: Boolean,
    val isObserved: Boolean,
    val canManageObservation: Boolean,
    val canSendPrivateMessage: Boolean,
)
