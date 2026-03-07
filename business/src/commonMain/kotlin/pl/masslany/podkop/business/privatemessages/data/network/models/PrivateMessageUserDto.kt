package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageUserDto(
    @SerialName("username")
    val username: String,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("color")
    val color: String? = null,
)
