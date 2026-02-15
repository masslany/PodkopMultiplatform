package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileShortDataDto(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("blacklist")
    val blacklist: Boolean,
    @SerialName("color")
    val color: String,
    @SerialName("company")
    val company: Boolean,
    @SerialName("follow")
    val follow: Boolean,
    @SerialName("gender")
    val gender: String,
    @SerialName("note")
    val note: Boolean,
    @SerialName("online")
    val online: Boolean,
    @SerialName("rank")
    val rank: RankDto,
    @SerialName("status")
    val status: String,
    @SerialName("username")
    val username: String,
    @SerialName("verified")
    val verified: Boolean,
)
