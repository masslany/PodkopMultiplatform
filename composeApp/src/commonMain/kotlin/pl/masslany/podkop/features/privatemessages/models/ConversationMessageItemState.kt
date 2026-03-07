package pl.masslany.podkop.features.privatemessages.models

import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState

data class ConversationMessageItemState(
    val key: String,
    val isIncoming: Boolean,
    val contentState: EntryContentState?,
    val embedImageState: EmbedImageState?,
    val embedUrl: String?,
    val adult: Boolean,
    val publishedAt: PublishedTimeType,
    val senderName: String?,
    val senderAvatarState: AvatarState,
    val senderNameColorType: NameColorType,
)
