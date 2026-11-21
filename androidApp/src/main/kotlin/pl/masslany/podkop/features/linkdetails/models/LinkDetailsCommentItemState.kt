package pl.masslany.podkop.features.linkdetails.models

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

data class LinkDetailsCommentItemState(
    val id: Int,
    val comment: LinkCommentItemState,
    val replies: ImmutableList<LinkCommentItemState>,
    val remainingRepliesCount: Int,
    val nextRepliesPage: Int?,
    val isLoadingReplies: Boolean,
)
