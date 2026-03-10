package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNoteResponseDto(
    @SerialName("data")
    val data: ProfileNoteDataDto,
)

@Serializable
data class ProfileNoteDataDto(
    @SerialName("content")
    val content: String = "",
)
