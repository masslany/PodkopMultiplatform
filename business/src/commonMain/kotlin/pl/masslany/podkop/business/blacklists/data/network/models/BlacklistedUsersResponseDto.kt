package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class BlacklistedUsersResponseDto(
    @SerialName("data")
    val data: List<BlacklistedUserDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class BlacklistedUserDto(
    @SerialName("username")
    val username: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("color")
    val color: String = "orange",
    @SerialName("avatar")
    val avatar: String = "",
)
