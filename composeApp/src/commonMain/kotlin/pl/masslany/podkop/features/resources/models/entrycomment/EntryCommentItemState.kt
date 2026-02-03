package pl.masslany.podkop.features.resources.models.entrycomment

import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class EntryCommentItemState(
    override val id: Int,
    val parentId: Int,
    val avatarState: AvatarState,
    val authorState: AuthorState?,
    val entryContentState: EntryContentState,
    val publishedTimeType: PublishedTimeType?,
    val voteState: VoteState,
    val embedImageState: EmbedImageState?,
) : ResourceItemState
