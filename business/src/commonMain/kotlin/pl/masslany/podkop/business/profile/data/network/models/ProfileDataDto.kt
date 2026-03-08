package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer

@Serializable
data class ProfileDataDto(
    @SerialName("about")
    val about: String,
    @SerialName("actions")
    val actions: ActionsDto,
    @SerialName("avatar")
    val avatar: String,
    @SerialName("background")
    val background: String,
    @SerialName("banned")
    val banned: BannedDto? = null,
    @SerialName("blacklist")
    val blacklist: Boolean = false,
    @SerialName("can_change_gender")
    val canChangeGender: Boolean? = null,
    @SerialName("city")
    val city: String,
    @SerialName("color")
    val color: String,
    @SerialName("company")
    val company: Boolean,
    @SerialName("follow")
    val follow: Boolean,
    @SerialName("followers")
    val followers: Int,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("member_since")
    @Serializable(with = DateAsStringSerializer::class)
    val memberSince: LocalDateTime?,
    @SerialName("name")
    val name: String,
    @SerialName("note")
    val note: Boolean,
    @SerialName("online")
    val online: Boolean,
    @SerialName("public_email")
    val publicEmail: String,
    @SerialName("rank")
    val rank: RankDto,
    @SerialName("social_media")
    val socialMedia: SocialMediaDto,
    @SerialName("status")
    val status: String,
    @SerialName("summary")
    val summary: SummaryDto,
    @SerialName("username")
    val username: String,
    @SerialName("verified")
    val verified: Boolean,
    @SerialName("website")
    val website: String,
)
