package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageOpenDataDto(
    @SerialName("content")
    val content: String,
    @SerialName("photo")
    val photo: String? = null,
    @SerialName("embed")
    val embed: String? = null,
)
