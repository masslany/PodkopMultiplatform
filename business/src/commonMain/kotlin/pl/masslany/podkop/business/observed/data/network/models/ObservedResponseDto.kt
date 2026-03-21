package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class ObservedResponseDto(
    @SerialName("data")
    @Serializable(with = ObservedItemsSerializer::class)
    val data: List<ObservedItemDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
