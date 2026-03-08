package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlacklistTagRequestDto(
    @SerialName("data")
    val data: BlacklistTagDataDto,
)

@Serializable
data class BlacklistTagDataDto(
    @SerialName("tag")
    val tag: String,
)
