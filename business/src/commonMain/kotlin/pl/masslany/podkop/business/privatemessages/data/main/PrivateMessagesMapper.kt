package pl.masslany.podkop.business.privatemessages.data.main

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageConversationDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage

internal fun PrivateMessagesListDto.toPrivateMessagesPage(): PrivateMessagesPage =
    PrivateMessagesPage(
        data = data.map(PrivateMessageConversationDto::toPrivateMessageConversation),
        pagination = pagination?.toPagination(),
    )

private fun PrivateMessageConversationDto.toPrivateMessageConversation(): PrivateMessageConversation =
    PrivateMessageConversation(
        username = user.username,
        avatarUrl = user.avatar?.takeIf { it.isNotBlank() },
        gender = user.gender.toGender(),
        nameColor = user.color?.toNameColor() ?: NameColor.Orange,
        lastMessageKey = lastMessage.key,
        lastMessageContent = lastMessage.content?.takeIf { it.isNotBlank() },
        lastMessageCreatedAt = lastMessage.createdAt,
        unread = unread,
    )
