package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessageConversationDto(
    @SerialName("user")
    val user: PrivateMessageUserDto,
    @SerialName("last_message")
    val lastMessage: PrivateMessageDto,
    @SerialName("unread")
    val unread: Boolean = false,
)
