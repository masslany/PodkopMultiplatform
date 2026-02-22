package pl.masslany.podkop.common.models.vote

import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted

fun ResourceItem.toVoteState(): VoteState {
    val actions = this.actions
    val showPositiveVoteButton =
        (actions?.voteUp == true) || (this.voted == Voted.Positive && actions?.undoVote == true)
    val showNegativeVoteButton = supportsNegativeVote(this.resource) &&
        ((actions?.voteDown == true) || (this.voted == Voted.Negative && actions?.undoVote == true))

    return VoteState(
        voteValueType = this.toVoteValueType(),
        positiveVoteButtonState = if (showPositiveVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Positive,
                isVoted = this.voted == Voted.Positive,
            )
        } else {
            null
        },
        negativeVoteButtonState = if (showNegativeVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Negative,
                isVoted = this.voted == Voted.Negative,
            )
        } else {
            null
        },
    )
}

fun Comment.toVoteState(): VoteState {
    val showPositiveVoteButton = this.actions.voteUp || (this.voted == Voted.Positive && this.actions.undoVote)
    val showNegativeVoteButton = supportsNegativeVote(this.resource) &&
        (this.actions.voteDown || (this.voted == Voted.Negative && this.actions.undoVote))

    return VoteState(
        voteValueType = this.toVoteValueType(),
        positiveVoteButtonState = if (showPositiveVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Positive,
                isVoted = this.voted == Voted.Positive,
            )
        } else {
            null
        },
        negativeVoteButtonState = if (showNegativeVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Negative,
                isVoted = this.voted == Voted.Negative,
            )
        } else {
            null
        },
    )
}

private fun supportsNegativeVote(resource: Resource): Boolean = when (resource) {
    Resource.Link,
    Resource.LinkComment,
    -> true

    Resource.Entry,
    Resource.EntryComment,
    Resource.Unknown,
    -> false
}
