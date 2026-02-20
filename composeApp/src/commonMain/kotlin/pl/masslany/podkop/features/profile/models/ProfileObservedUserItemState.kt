package pl.masslany.podkop.features.profile.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType

data class ProfileObservedUserItemState(
    val username: String,
    val avatarUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
    val online: Boolean,
    val company: Boolean,
    val verified: Boolean,
    val status: String,
)
