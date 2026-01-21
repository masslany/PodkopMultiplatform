package pl.masslany.podkop.business.common.data.network.models.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable
data class EntriesDto(
    @SerialName("data")
    val data: List<ResourceItemDto>,
    @SerialName("pagination")
    val pagination: PaginationDto,
)
