package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNoteUpdateRequestDto(
    @SerialName("data")
    val data: ProfileNoteUpdateDataDto,
)

@Serializable
data class ProfileNoteUpdateDataDto(
    @SerialName("content")
    val content: String,
)
