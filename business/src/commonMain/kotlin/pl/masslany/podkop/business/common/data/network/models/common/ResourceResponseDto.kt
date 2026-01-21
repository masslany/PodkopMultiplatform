package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResourceResponseDto(
    @SerialName("data")
    val data: List<ResourceItemDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
