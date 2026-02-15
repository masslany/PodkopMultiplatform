package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersAutoCompleteDataDto(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("color")
    val color: String,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("username")
    val username: String,
)
