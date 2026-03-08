package pl.masslany.podkop.features.resources.models.entrycomment

import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType

data class EntryCommentItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val parentId: Int,
    val avatarState: AvatarState,
    val authorState: AuthorState?,
    val isBlacklisted: Boolean = false,
    val entryContentState: EntryContentState,
    val publishedTimeType: PublishedTimeType?,
    val voteState: VoteState,
    val isFavourite: Boolean = false,
    val isFavouriteEnabled: Boolean = false,
    val isDeleteEnabled: Boolean = false,
    val isEditEnabled: Boolean = false,
    val rawContent: String = "",
    val adult: Boolean = false,
    val embedImageState: EmbedImageState?,
    val embedContentState: EmbedContentState? = null,
) : ResourceItemState
