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
    @SerialName("photo")
    val photo: String? = null,
)

@Serializable
data class EntryThreadReplyCreateRequestDto(
    @SerialName("data")
    val data: EntryThreadReplyCreateDataDto,
)

/** Thread endpoints take uploaded photo keys as a list, unlike the single `photo` of the flat ones. */
@Serializable
data class EntryThreadReplyCreateDataDto(
    @SerialName("content")
    val content: String,
    @SerialName("adult")
    val adult: Boolean,
    @SerialName("photos")
    val photos: List<String>? = null,
)
