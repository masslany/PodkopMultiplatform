package pl.masslany.podkop.business.privatemessages.domain.models

import kotlinx.datetime.LocalDateTime

data class PrivateMessage(
    val key: String,
    val content: String?,
    val createdAt: LocalDateTime,
    val isRead: Boolean,
    val adult: Boolean,
    val type: Int?,
    val sender: PrivateMessageSender?,
    val mediaPhotoUrl: String?,
    val mediaEmbedUrl: String?,
)

