package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VotesDto(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("down")
    val down: Int,
    @SerialName("up")
    val up: Int,
    @SerialName("users")
    val users: List<UserDto>? = null,
)
