package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class PrivateMessageThreadDto(
    @SerialName("data")
    val data: List<PrivateMessageDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
