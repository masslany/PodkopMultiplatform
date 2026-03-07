package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer

@Serializable
data class PrivateMessageDto(
    @SerialName("content")
    val content: String? = null,
    @SerialName("adult")
    val adult: Boolean = false,
    @SerialName("type")
    val type: Int? = null,
    @SerialName("created_at")
    @Serializable(with = DateAsStringSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("media")
    val media: PrivateMessageMediaDto? = null,
    @SerialName("read")
    val read: Boolean = false,
    @SerialName("key")
    val key: String,
    @SerialName("user")
    val user: PrivateMessageUserDto? = null,
)
