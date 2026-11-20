package pl.masslany.podkop.common.models.vote

import androidx.annotation.StringRes
import pl.masslany.podkop.composeapp.R

enum class VoteReasonType {
    Duplicate,
    Spam,
    Fake,
    Wrong,
    Invalid,
}

@StringRes
fun VoteReasonType.toStringResource(): Int = when (this) {
    VoteReasonType.Duplicate -> R.string.vote_reason_duplicate
    VoteReasonType.Spam -> R.string.vote_reason_spam
    VoteReasonType.Fake -> R.string.vote_reason_fake
    VoteReasonType.Wrong -> R.string.vote_reason_wrong
    VoteReasonType.Invalid -> R.string.vote_reason_invalid
}
