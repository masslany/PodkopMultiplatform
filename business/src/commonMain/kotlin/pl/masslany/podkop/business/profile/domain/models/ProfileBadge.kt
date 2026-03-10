package pl.masslany.podkop.business.profile.domain.models

import kotlinx.datetime.LocalDateTime

data class ProfileBadge(
    val label: String,
    val slug: String,
    val description: String,
    val iconUrl: String,
    val iconMimeType: String,
    val colorHex: String,
    val colorHexDark: String,
    val level: Int?,
    val progress: Int?,
    val achievedAt: LocalDateTime?,
)
