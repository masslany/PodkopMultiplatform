package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable(with = ObservedItemDtoSerializer::class)
data class ObservedItemDto(
    val type: String? = null,
    val item: ResourceItemDto,
    val newContentCount: Int? = null,
)
