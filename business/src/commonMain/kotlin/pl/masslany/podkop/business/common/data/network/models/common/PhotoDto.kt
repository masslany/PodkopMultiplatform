package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    @SerialName("height")
    val height: Int,
    @SerialName("key")
    val key: String,
    @SerialName("label")
    val label: String,
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("size")
    val size: Int,
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int,
)
