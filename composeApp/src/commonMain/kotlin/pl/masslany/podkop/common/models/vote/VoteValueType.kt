package pl.masslany.podkop.common.models.vote

import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

sealed class VoteValueType {
    data class Positive(val value: String): VoteValueType()

    data class Negative(val value: String): VoteValueType()

    data object Zero : VoteValueType()

    fun increaseVoteUp(): VoteValueType {
        return when (this) {
            is Zero -> Positive("1")
            is Positive -> Positive((this.value.toInt() + 1).toString())
            is Negative -> {
                if (this.value.toInt() == -1) {
                    Zero
                } else {
                    Negative((this.value.toInt() + 1).toString())
                }
            }
        }
    }

    fun increaseVoteDown(): VoteValueType {
        return when (this) {
            is Zero -> Negative("-1")
            is Positive -> {
                if (this.value.toInt() == 1) {
                    Zero
                } else {
                    Positive((this.value.toInt() - 1).toString())
                }
            }
            is Negative -> Negative((this.value.toInt() - 1).toString())
        }
    }

    fun removeVoteUp(): VoteValueType {
        return when (this) {
            is Zero -> Negative("-1")
            is Positive -> {
                if (this.value.toInt() == 1) {
                    Zero
                } else {
                    Positive((this.value.toInt() - 1).toString())
                }
            }
            is Negative -> Negative((this.value.toInt() - 1).toString())
        }
    }

    fun removeVoteDown(): VoteValueType {
        return when (this) {
            is Zero -> Positive("1")
            is Positive -> Positive((this.value.toInt() + 1).toString())
            is Negative -> {
                if (this.value.toInt() == -1) {
                    Zero
                } else {
                    Negative((this.value.toInt() + 1).toString())
                }
            }
        }
    }
}

fun ResourceItem.toVoteValueType(): VoteValueType {
    val voteCount = (this.votes?.up ?: 0) - (this.votes?.down ?: 0)

    return if (voteCount == 0) {
        VoteValueType.Zero
    } else if (voteCount > 0) {
        VoteValueType.Positive(voteCount.toString())
    } else {
        VoteValueType.Negative(voteCount.toString())
    }
}

fun Comment.toVoteValueType(): VoteValueType {
    val voteCount = this.votes.up - this.votes.down

    return if (voteCount == 0) {
        VoteValueType.Zero
    } else if (voteCount > 0) {
        VoteValueType.Positive(voteCount.toString())
    } else {
        VoteValueType.Negative(voteCount.toString())
    }
}

