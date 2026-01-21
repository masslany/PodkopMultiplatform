package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleResourceResponseDto(
    @SerialName("data")
    val data: ResourceItemDto,
)
