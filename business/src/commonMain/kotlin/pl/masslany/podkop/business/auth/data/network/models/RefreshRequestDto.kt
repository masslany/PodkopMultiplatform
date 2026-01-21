package pl.masslany.podkop.business.auth.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequestDto(
    @SerialName("data")
    val data: RefreshRequestData,
)

@Serializable
data class RefreshRequestData(
    @SerialName("refresh_token")
    val refreshToken: String,
)
