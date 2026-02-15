package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannedDto(
    @SerialName("expired")
    val expired: String,
    @SerialName("reason")
    val reason: String,
)
