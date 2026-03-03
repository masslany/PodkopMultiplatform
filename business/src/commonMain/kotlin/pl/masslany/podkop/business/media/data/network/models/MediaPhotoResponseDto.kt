package pl.masslany.podkop.business.media.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto

@Serializable
data class MediaPhotoResponseDto(
    @SerialName("data")
    val data: PhotoDto,
)
