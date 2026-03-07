package pl.masslany.podkop.business.privatemessages.domain.models

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class PrivateMessageConversation(
    val username: String,
    val avatarUrl: String?,
    val gender: Gender,
    val nameColor: NameColor,
    val lastMessageKey: String,
    val lastMessageContent: String?,
    val lastMessageCreatedAt: LocalDateTime,
    val unread: Boolean,
)


