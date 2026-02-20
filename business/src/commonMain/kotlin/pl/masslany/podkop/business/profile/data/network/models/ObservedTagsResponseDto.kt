package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class ObservedTagsResponseDto(
    @SerialName("data")
    val data: List<ObservedTagDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
