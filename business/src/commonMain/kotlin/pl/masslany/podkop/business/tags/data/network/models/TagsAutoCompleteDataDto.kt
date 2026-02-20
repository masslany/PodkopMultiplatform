package pl.masslany.podkop.business.tags.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagsAutoCompleteDataDto(
    @SerialName("name")
    val name: String,
    @SerialName("observed_qty")
    val observedQuantity: Int,
)
