package pl.masslany.podkop.business.common.domain.models.common

data class Actions(
    val create: Boolean,
    val createFavourite: Boolean,
    val delete: Boolean,
    val deleteFavourite: Boolean,
    val finishAma: Boolean,
    val report: Boolean,
    val startAma: Boolean,
    val undoVote: Boolean,
    val update: Boolean,
    val voteDown: Boolean,
    val voteUp: Boolean,
)
