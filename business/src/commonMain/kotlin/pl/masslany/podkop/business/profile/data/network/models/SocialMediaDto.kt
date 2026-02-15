package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocialMediaDto(
    @SerialName("facebook")
    val facebook: String,
    @SerialName("instagram")
    val instagram: String,
    @SerialName("twitter")
    val twitter: String,
)
