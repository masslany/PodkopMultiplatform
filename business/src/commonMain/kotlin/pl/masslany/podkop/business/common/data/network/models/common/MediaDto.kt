package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.entries.SurveyDto

@Serializable
data class MediaDto(
    @SerialName("embed")
    val embed: EmbedDto? = null,
    @SerialName("photo")
    val photo: PhotoDto? = null,
    @SerialName("survey")
    val survey: SurveyDto? = null,
)
