package pl.masslany.podkop.business.profile.domain.models

import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class ProfileShort(
    val name: String,
    val avatarUrl: String,
    val gender: Gender,
    val color: NameColor,
)
