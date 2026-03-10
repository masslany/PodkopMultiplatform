package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import pl.masslany.podkop.business.profile.domain.models.ProfileBadge

internal fun List<ProfileBadge>.toProfileAchievementItemStates(): ImmutableList<ProfileAchievementItemState> =
    map { badge ->
        ProfileAchievementItemState(
            label = badge.label,
            slug = badge.slug,
            description = badge.description,
            iconUrl = badge.iconUrl,
            iconMimeType = badge.iconMimeType,
            colorHex = badge.colorHex,
            colorHexDark = badge.colorHexDark,
            level = badge.level,
            progress = badge.progress,
            achievedAt = badge.achievedAt.toAchievementDateLabel(),
        )
    }.toImmutableList()

private fun LocalDateTime?.toAchievementDateLabel(): String {
    val achievementDateFormat = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
    }
    return this?.let { achievementDateFormat.format(it) } ?: ""
}
