package pl.masslany.podkop.common.models.vote

import org.jetbrains.compose.resources.StringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.vote_reason_duplicate
import podkop.composeapp.generated.resources.vote_reason_fake
import podkop.composeapp.generated.resources.vote_reason_invalid
import podkop.composeapp.generated.resources.vote_reason_spam
import podkop.composeapp.generated.resources.vote_reason_wrong

enum class VoteReasonType {
    Duplicate,
    Spam,
    Fake,
    Wrong,
    Invalid,
}

fun VoteReasonType.toStringResource(): StringResource = when (this) {
    VoteReasonType.Duplicate -> Res.string.vote_reason_duplicate
    VoteReasonType.Spam -> Res.string.vote_reason_spam
    VoteReasonType.Fake -> Res.string.vote_reason_fake
    VoteReasonType.Wrong -> Res.string.vote_reason_wrong
    VoteReasonType.Invalid -> Res.string.vote_reason_invalid
}
