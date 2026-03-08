package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlacklistUserRequestDto(
    @SerialName("data")
    val data: BlacklistUserDataDto,
)

@Serializable
data class BlacklistUserDataDto(
    @SerialName("username")
    val username: String,
)
