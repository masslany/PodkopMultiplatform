package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    @SerialName("label")
    val label: String,
    @SerialName("type")
    val type: String? = null,
    @SerialName("type_id")
    val typeId: Int? = null,
    @SerialName("url")
    val url: String,
)
