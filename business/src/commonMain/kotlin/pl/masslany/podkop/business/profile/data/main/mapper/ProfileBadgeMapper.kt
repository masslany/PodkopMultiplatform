package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.profile.data.network.models.ProfileBadgeDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileBadgesResponseDto
import pl.masslany.podkop.business.profile.domain.models.ProfileBadge

fun ProfileBadgesResponseDto.toProfileBadges(): List<ProfileBadge> = data.map(ProfileBadgeDto::toProfileBadge)

private fun ProfileBadgeDto.toProfileBadge(): ProfileBadge = ProfileBadge(
    label = label,
    slug = slug,
    description = description,
    iconUrl = media?.icon?.url.orEmpty(),
    iconMimeType = media?.icon?.mimeType.orEmpty(),
    colorHex = color?.hex.orEmpty(),
    colorHexDark = color?.hexDark.orEmpty(),
    level = level,
    progress = progress,
    achievedAt = achievedAt,
)
