package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.data.network.models.common.UserDto

@Serializable
data class ObservedUsersResponseDto(
    @SerialName("data")
    val data: List<UserDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
