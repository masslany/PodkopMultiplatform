package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable
data class LinkDraftCheckResponseDto(
    @SerialName("data")
    val data: LinkDraftCheckDataDto,
)

@Serializable
data class LinkDraftCheckDataDto(
    @SerialName("key")
    val key: String,
    @SerialName("similar")
    val similar: List<ResourceItemDto> = emptyList(),
    @SerialName("duplicate")
    val duplicate: Boolean,
)
