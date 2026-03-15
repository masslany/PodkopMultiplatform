package pl.masslany.podkop.features.linkdetails

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.vote.VoteReasonType

data class LinkDownvoteMenuState(
    val reasons: ImmutableList<VoteReasonType>,
    val isVisible: Boolean,
    val expanded: Boolean,
    val isSubmitting: Boolean,
) {
    companion object {
        val initial = LinkDownvoteMenuState(
            reasons = persistentListOf(
                VoteReasonType.Duplicate,
                VoteReasonType.Spam,
                VoteReasonType.Fake,
                VoteReasonType.Wrong,
                VoteReasonType.Invalid,
            ),
            isVisible = false,
            expanded = false,
            isSubmitting = false,
        )
    }
}
