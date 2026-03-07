package pl.masslany.podkop.business.privatemessages.domain.models

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Embed
import pl.masslany.podkop.business.common.domain.models.common.Photo

data class PrivateMessage(
    val key: String,
    val content: String?,
    val createdAt: LocalDateTime,
    val isRead: Boolean,
    val adult: Boolean,
    val type: Int?,
    val sender: PrivateMessageSender?,
    val photo: Photo?,
    val embed: Embed?,
)
