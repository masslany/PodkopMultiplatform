package pl.masslany.podkop.business.rank.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class RankResponseDto(
    @SerialName("data")
    val data: List<RankEntryDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)
