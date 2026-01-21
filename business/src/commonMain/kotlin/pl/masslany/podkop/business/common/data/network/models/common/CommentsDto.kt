package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.comments.CommentItemDto

@Serializable
data class CommentsDto(
    @SerialName("count")
    val count: Int,
    @SerialName("hot")
    val hot: Boolean? = null,
    @SerialName("items")
    val items: List<CommentItemDto>? = emptyList(),
)
