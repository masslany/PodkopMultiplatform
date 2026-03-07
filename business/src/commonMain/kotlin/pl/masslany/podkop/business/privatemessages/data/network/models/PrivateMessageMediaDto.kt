package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.EmbedDto
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto

@Serializable
data class PrivateMessageMediaDto(
    @SerialName("photo")
    val photo: PhotoDto? = null,
    @SerialName("embed")
    val embed: EmbedDto? = null,
)
