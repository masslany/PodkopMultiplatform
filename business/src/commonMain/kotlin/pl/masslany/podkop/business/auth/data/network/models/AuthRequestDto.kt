package pl.masslany.podkop.business.auth.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    @SerialName("data")
    val data: AuthRequestData,
)

@Serializable
data class AuthRequestData(
    val key: String,
    val secret: String,
)
