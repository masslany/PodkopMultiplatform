package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageOpenDataDto(
    @SerialName("username")
    val username: String,
    @SerialName("content")
    val content: String,
    @SerialName("adult")
    val adult: Boolean = false,
    @SerialName("photo")
    val photo: String? = null,
    @SerialName("embed")
    val embed: String? = null,
)
