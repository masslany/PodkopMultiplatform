package pl.masslany.podkop.common.models.vote

import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem


fun ResourceItem.toVoteState(): VoteState  {
    val showPositiveVoteButton = this.actions?.voteUp == true
    val showNegativeVoteButton = this.actions?.voteDown == true


    return VoteState(
        voteValueType = this.toVoteValueType(),
        positiveVoteButtonState = if (showPositiveVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Positive,
                isVoted = !(this.actions?.voteUp ?: false) &&
                        this.actions?.undoVote ?: false,
            )
        } else {
            null
        },
        negativeVoteButtonState = if (showNegativeVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Negative,
                isVoted = !(this.actions?.voteDown ?: false) &&
                        this.actions?.undoVote ?: false,
            )
        } else {
             null
        },
    )
}

fun Comment.toVoteState(): VoteState  {
    val showPositiveVoteButton = this.actions.voteUp
    val showNegativeVoteButton = this.actions.voteDown


    return VoteState(
        voteValueType = this.toVoteValueType(),
        positiveVoteButtonState = if (showPositiveVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Positive,
                isVoted = !this.actions.voteUp &&
                        this.actions.undoVote,
            )
        } else {
            null
        },
        negativeVoteButtonState = if (showNegativeVoteButton) {
            VoteButtonState(
                voteButtonType = VoteButtonType.Negative,
                isVoted = !this.actions.voteDown &&
                        this.actions.undoVote,
            )
        } else {
            null
        },
    )
}
