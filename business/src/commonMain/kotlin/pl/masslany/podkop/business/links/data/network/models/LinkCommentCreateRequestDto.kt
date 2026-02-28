package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkCommentCreateRequestDto(
    @SerialName("data")
    val data: LinkCommentCreateDataDto,
)

@Serializable
data class LinkCommentCreateDataDto(
    @SerialName("content")
    val content: String,
    @SerialName("adult")
    val adult: Boolean,
)
