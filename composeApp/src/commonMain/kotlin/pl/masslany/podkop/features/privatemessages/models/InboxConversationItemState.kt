package pl.masslany.podkop.features.privatemessages.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState

data class InboxConversationItemState(
    val username: String,
    val avatarState: AvatarState,
    val nameColorType: NameColorType,
    val lastMessagePreview: String,
    val publishedAt: PublishedTimeType,
    val unread: Boolean,
)
