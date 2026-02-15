package pl.masslany.podkop.business.profile.domain.models

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class Profile(
    val name: String,
    val avatarUrl: String,
    val gender: Gender,
    val color: NameColor,
    val backgroundUrl: String,
    val summary: Summary,
    val memberSince: LocalDateTime?,
)
