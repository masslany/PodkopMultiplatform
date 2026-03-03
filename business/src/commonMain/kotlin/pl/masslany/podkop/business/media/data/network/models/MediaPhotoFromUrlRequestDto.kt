package pl.masslany.podkop.business.media.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaPhotoFromUrlRequestDto(
    @SerialName("data")
    val data: MediaPhotoFromUrlDataDto,
)

@Serializable
data class MediaPhotoFromUrlDataDto(
    @SerialName("url")
    val url: String,
)
