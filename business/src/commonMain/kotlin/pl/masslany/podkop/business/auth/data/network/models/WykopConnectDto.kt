package pl.masslany.podkop.business.auth.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WykopConnectDto(
    @SerialName("data")
    val data: WykopConnectDtoData,
)

@Serializable
data class WykopConnectDtoData(
    @SerialName("connect_url")
    val connectUrl: String,
)
