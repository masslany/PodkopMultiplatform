package pl.masslany.podkop.business.tags.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDetailsResponseDto(
    @SerialName("data")
    val data: TagDetailsDto,
)
