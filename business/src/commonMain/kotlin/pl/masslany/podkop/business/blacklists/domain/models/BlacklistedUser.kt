package pl.masslany.podkop.business.blacklists.domain.models

import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class BlacklistedUser(
    val username: String,
    val createdAt: String,
    val gender: Gender,
    val color: NameColor,
    val avatarUrl: String,
)
