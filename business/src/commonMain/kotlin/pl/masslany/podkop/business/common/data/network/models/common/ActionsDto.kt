package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActionsDto(
    @SerialName("create")
    val create: Boolean? = null,
    @SerialName("create_favourite")
    val createFavourite: Boolean? = null,
    @SerialName("delete")
    val delete: Boolean? = null,
    @SerialName("delete_favourite")
    val deleteFavourite: Boolean? = null,
    @SerialName("finish_ama")
    val finishAma: Boolean? = null,
    @SerialName("report")
    val report: Boolean? = null,
    @SerialName("start_ama")
    val startAma: Boolean? = null,
    @SerialName("undo_vote")
    val undoVote: Boolean? = null,
    @SerialName("update")
    val update: Boolean? = null,
    @SerialName("vote_down")
    val voteDown: Boolean? = null,
    @SerialName("vote_up")
    val voteUp: Boolean? = null,
    @SerialName("vote")
    val vote: Boolean? = null,
)
