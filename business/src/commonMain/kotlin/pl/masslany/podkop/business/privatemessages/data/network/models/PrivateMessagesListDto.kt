package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class PrivateMessagesListDto(
    @SerialName("data")
    val data: List<PrivateMessageConversationDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class PrivateMessageConversationDto(
    @SerialName("user")
    val user: PrivateMessageUserDto,
    @SerialName("last_message")
    val lastMessage: PrivateMessageDto,
    @SerialName("unread")
    val unread: Boolean = false,
)

@Serializable
data class PrivateMessageUserDto(
    @SerialName("username")
    val username: String,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("color")
    val color: String? = null,
)

@Serializable
data class PrivateMessageDto(
    @SerialName("content")
    val content: String? = null,
    @SerialName("created_at")
    @Serializable(with = DateAsStringSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("key")
    val key: String,
)
