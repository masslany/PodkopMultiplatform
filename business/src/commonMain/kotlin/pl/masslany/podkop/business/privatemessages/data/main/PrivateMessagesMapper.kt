package pl.masslany.podkop.business.privatemessages.data.main

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageConversationDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageUserDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageSender
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageThreadPage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage

internal fun PrivateMessagesListDto.toPrivateMessagesPage(): PrivateMessagesPage =
    PrivateMessagesPage(
        data = data.map(PrivateMessageConversationDto::toPrivateMessageConversation),
        pagination = pagination?.toPagination(),
    )

internal fun PrivateMessageThreadDto.toPrivateMessageThreadPage(): PrivateMessageThreadPage =
    PrivateMessageThreadPage(
        data = data.map(PrivateMessageDto::toPrivateMessage),
        pagination = pagination?.toPagination(),
    )

internal fun PrivateMessageItemResponseDto.toPrivateMessage(): PrivateMessage = data.toPrivateMessage()

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

private fun PrivateMessageDto.toPrivateMessage(): PrivateMessage =
    PrivateMessage(
        key = key,
        content = content?.takeIf { it.isNotBlank() },
        createdAt = createdAt,
        isRead = read,
        adult = adult,
        type = type,
        sender = user?.toPrivateMessageSender(),
        mediaPhotoUrl = media?.photo?.takeIf { it.isNotBlank() },
        mediaEmbedUrl = media?.embed?.takeIf { it.isNotBlank() },
    )

private fun PrivateMessageUserDto.toPrivateMessageSender(): PrivateMessageSender =
    PrivateMessageSender(
        username = username,
        avatarUrl = avatar?.takeIf { it.isNotBlank() },
        gender = gender.toGender(),
        nameColor = color?.toNameColor() ?: NameColor.Orange,
    )
