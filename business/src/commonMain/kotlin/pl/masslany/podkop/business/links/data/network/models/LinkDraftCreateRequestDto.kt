package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDraftCreateRequestDto(
    @SerialName("data")
    val data: LinkDraftCreateDataDto,
)

@Serializable
data class LinkDraftCreateDataDto(
    @SerialName("url")
    val url: String,
)
