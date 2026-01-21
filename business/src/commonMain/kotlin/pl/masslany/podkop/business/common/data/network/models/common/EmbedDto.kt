package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbedDto(
    @SerialName("key")
    val key: String,
    @SerialName("thumbnail")
    val thumbnail: String?,
    @SerialName("type")
    val type: String,
    @SerialName("url")
    val url: String,
)
