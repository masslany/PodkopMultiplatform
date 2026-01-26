package pl.masslany.podkop.common.models

data class CountState(
    val count: String,
    val isHot: Boolean,
    val isVoted: Boolean,
    val canVote: Boolean,
)
