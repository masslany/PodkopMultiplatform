package pl.masslany.podkop.features.privatemessages.models

import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.isGifImage
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType

internal fun List<PrivateMessageConversation>.toInboxConversationItemStates(): List<InboxConversationItemState> =
    map { conversation ->
        val avatarUrl = conversation.avatarUrl
        val avatarType = if (avatarUrl != null) {
            AvatarType.NetworkImage(avatarUrl)
        } else {
            AvatarType.NoAvatar
        }

        val avatarState = AvatarState(
            type = avatarType,
            genderIndicatorType = conversation.gender.toGenderIndicatorType(),
        )

        InboxConversationItemState(
            username = conversation.username,
            avatarState = avatarState,
            nameColorType = conversation.nameColor.toNameColorType(),
            lastMessagePreview = conversation.lastMessageContent.orEmpty(),
            publishedAt = conversation.lastMessageCreatedAt.toPublishedTimeType(),
            unread = conversation.unread,
        )
    }

internal fun List<PrivateMessage>.toConversationMessageItemStates(): List<ConversationMessageItemState> = map { message ->
    val sender = message.sender

    val avatarUrl = sender?.avatarUrl
    val avatarType = if (avatarUrl != null) {
        AvatarType.NetworkImage(avatarUrl)
    } else {
        AvatarType.NoAvatar
    }

    val avatarState = AvatarState(
        type = avatarType,
        genderIndicatorType = sender?.gender?.toGenderIndicatorType() ?: GenderIndicatorType.Unspecified,
    )

    ConversationMessageItemState(
        key = message.key,
        isIncoming = message.type == 1,
        contentState = message.content
            ?.takeIf(String::isNotBlank)
            ?.toEntryContentState(isDownVoted = false),
        embedImageState = message.photo?.let { photo ->
            EmbedImageState(
                url = photo.url,
                key = photo.key,
                source = photo.label,
                isAdult = message.adult,
                isGif = isGifImage(
                    url = photo.url,
                    mimeType = photo.mimeType,
                ),
                width = photo.width,
                height = photo.height,
            )
        },
        embedUrl = message.embed?.url,
        adult = message.adult,
        publishedAt = message.createdAt.toPublishedTimeType(),
        senderName = sender?.username,
        senderAvatarState = avatarState,
        senderNameColorType = sender?.nameColor?.toNameColorType() ?: NameColorType.Orange,
    )
}

internal fun mergePrivateConversationMessages(
    existing: List<PrivateMessage>,
    incoming: List<PrivateMessage>,
): List<PrivateMessage> {
    val merged = LinkedHashMap<String, PrivateMessage>(existing.size + incoming.size)
    existing.forEach { message ->
        merged[message.key] = message
    }
    incoming.forEach { message ->
        merged[message.key] = message
    }

    return merged.values.sortedWith(compareBy({ it.createdAt }, { it.key }))
}

internal fun String.normalizePrivateMessageUsername(): String = trim().removePrefix("@")
