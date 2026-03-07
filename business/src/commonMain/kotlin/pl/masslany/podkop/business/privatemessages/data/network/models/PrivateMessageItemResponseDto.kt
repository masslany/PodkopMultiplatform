package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageItemResponseDto(
    @SerialName("data")
    val data: PrivateMessageDto,
)
