package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class BlacklistedTagsResponseDto(
    @SerialName("data")
    val data: List<BlacklistedTagDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class BlacklistedTagDto(
    @SerialName("name")
    val name: String,
    @SerialName("created_at")
    val createdAt: String,
)
