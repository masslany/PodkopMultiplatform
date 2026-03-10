package pl.masslany.podkop.features.profile.models

data class ProfileAchievementItemState(
    val label: String,
    val slug: String,
    val description: String,
    val iconUrl: String,
    val iconMimeType: String,
    val colorHex: String,
    val colorHexDark: String,
    val level: Int?,
    val progress: Int?,
    val achievedAt: String,
)
