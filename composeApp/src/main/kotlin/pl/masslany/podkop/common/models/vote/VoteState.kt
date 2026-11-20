package pl.masslany.podkop.common.models.vote

data class VoteState(
    val voteValueType: VoteValueType,
    val positiveVoteButtonState: VoteButtonState?,
    val negativeVoteButtonState: VoteButtonState?,
) {
    fun increaseVoteUp(): VoteState = this.copy(
        voteValueType = voteValueType.increaseVoteUp(),
        positiveVoteButtonState = positiveVoteButtonState?.copy(
            isVoted = true,
        ),
    )
    fun removeVoteUp(): VoteState = this.copy(
        voteValueType = voteValueType.removeVoteUp(),
        positiveVoteButtonState = positiveVoteButtonState?.copy(
            isVoted = false,
        ),
    )
    fun increaseVoteDown(): VoteState = this.copy(
        voteValueType = voteValueType.increaseVoteDown(),
        negativeVoteButtonState = negativeVoteButtonState?.copy(
            isVoted = true,
        ),
    )
    fun removeVoteDown(): VoteState = this.copy(
        voteValueType = voteValueType.removeVoteDown(),
        negativeVoteButtonState = negativeVoteButtonState?.copy(
            isVoted = false,
        ),
    )
}
