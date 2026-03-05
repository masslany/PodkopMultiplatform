package pl.masslany.podkop.features.resources.models.entry

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.survey.SurveyState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState

data class EntryItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val avatarState: AvatarState,
    val authorState: AuthorState?,
    val totalCommentsCount: Int,
    val comments: ImmutableList<EntryCommentItemState>,
    val entryContentState: EntryContentState,
    val publishedTimeType: PublishedTimeType?,
    val voteState: VoteState,
    val isFavourite: Boolean = false,
    val isFavouriteEnabled: Boolean = false,
    val isDeleteEnabled: Boolean = false,
    val isEditEnabled: Boolean = false,
    val rawContent: String = "",
    val adult: Boolean = false,
    val photoKey: String? = null,
    val photoUrl: String? = null,
    val surveyState: SurveyState? = null,
    val embedImageState: EmbedImageState?,
    val embedContentState: EmbedContentState? = null,
) : ResourceItemState {
    val isShowCommentsButtonVisible: Boolean
        get() = comments.isNotEmpty() && totalCommentsCount > comments.size
}
