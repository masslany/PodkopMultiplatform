package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class ProfileBadgesResponseDto(
    @SerialName("data")
    val data: List<ProfileBadgeDto> = emptyList(),
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class ProfileBadgeDto(
    @SerialName("label")
    val label: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("description")
    val description: String,
    @SerialName("media")
    val media: ProfileBadgeMediaDto? = null,
    @SerialName("color")
    val color: ProfileBadgeColorDto? = null,
    @SerialName("level")
    val level: Int? = null,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("achieved_at")
    @Serializable(with = DateAsStringSerializer::class)
    val achievedAt: LocalDateTime? = null,
)

@Serializable
data class ProfileBadgeMediaDto(
    @SerialName("icon")
    val icon: ProfileBadgeIconDto? = null,
)

@Serializable
data class ProfileBadgeIconDto(
    @SerialName("url")
    val url: String = "",
    @SerialName("mime_type")
    val mimeType: String = "",
)

@Serializable
data class ProfileBadgeColorDto(
    @SerialName("hex")
    val hex: String = "",
    @SerialName("hex_dark")
    val hexDark: String = "",
)
