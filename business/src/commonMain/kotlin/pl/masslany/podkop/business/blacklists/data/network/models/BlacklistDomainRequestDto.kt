package pl.masslany.podkop.business.blacklists.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlacklistDomainRequestDto(
    @SerialName("data")
    val data: BlacklistDomainDataDto,
)

@Serializable
data class BlacklistDomainDataDto(
    @SerialName("domain")
    val domain: String,
)
