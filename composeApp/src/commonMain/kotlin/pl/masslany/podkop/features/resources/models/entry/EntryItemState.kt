package pl.masslany.podkop.features.resources.models.entry

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState

data class EntryItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val avatarState: AvatarState,
    val authorState: AuthorState?,
    val comments: ImmutableList<EntryCommentItemState>,
    val entryContentState: EntryContentState,
    val publishedTimeType: PublishedTimeType?,
    val voteState: VoteState,
    val embedImageState: EmbedImageState?,
//    val replies: ImmutableList<CommentItemState>,
//    val repliesPaginationState: CommentPaginationState,
//    val showMoreCommentsButtonState: ShowMoreCommentsButtonState,
//    val surveyState: SurveyState,
) : ResourceItemState
