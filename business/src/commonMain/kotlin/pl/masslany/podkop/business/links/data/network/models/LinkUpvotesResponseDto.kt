package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.data.network.models.common.UserDto

@Serializable
data class LinkUpvotesResponseDto(
    @SerialName("data")
    val data: List<LinkUpvoteItemDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class LinkUpvoteItemDto(
    @SerialName("user")
    val user: UserDto,
    @SerialName("reason")
    val reason: String = "",
)
