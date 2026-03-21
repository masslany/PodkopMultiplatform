package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable
data class ObservedWrappedItemDto(
    @SerialName("type")
    val type: String,
    @SerialName("object")
    val item: ResourceItemDto,
    @SerialName("new_content_count")
    val newContentCount: Int? = null,
)
