package pl.masslany.podkop.business.entries.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntryCreateRequestDto(
    @SerialName("data")
    val data: EntryCreateDataDto,
)

@Serializable
data class EntryCreateDataDto(
    @SerialName("content")
    val content: String,
    @SerialName("adult")
    val adult: Boolean,
    @SerialName("photo")
    val photo: String? = null,
)
