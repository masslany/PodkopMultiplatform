package pl.masslany.podkop.business.entries.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryCommentCreateRequestDto(
    @SerialName("data")
    val data: EntryCommentCreateDataDto,
)

@Serializable
data class EntryCommentCreateDataDto(
    @SerialName("content")
    val content: String,
    @SerialName("adult")
    val adult: Boolean,
)
