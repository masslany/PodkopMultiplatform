package pl.masslany.podkop.features.resources.models.linkcomment

import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType

data class LinkCommentItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val linkId: Int,
    val parentId: Int,
    val avatarState: AvatarState,
    val authorState: AuthorState?,
    val entryContentState: EntryContentState,
    val publishedTimeType: PublishedTimeType?,
    val voteState: VoteState,
    val embedImageState: EmbedImageState?,
) : ResourceItemState
