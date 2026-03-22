package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class BlacklistedDomainsResponseDto(
    @SerialName("data")
    val data: List<BlacklistedDomainDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class BlacklistedDomainDto(
    @SerialName("domain")
    val domain: String,
    @SerialName("created_at")
    val createdAt: String,
)
