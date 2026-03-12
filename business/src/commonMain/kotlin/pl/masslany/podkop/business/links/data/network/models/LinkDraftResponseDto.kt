package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.MediaDto

@Serializable
data class LinkDraftResponseDto(
    @SerialName("data")
    val data: LinkDraftDto,
)

@Serializable
data class LinkDraftsResponseDto(
    @SerialName("data")
    val data: List<LinkDraftDto>,
)

@Serializable
data class LinkDraftDto(
    @SerialName("key")
    val key: String,
    @SerialName("url")
    val url: String,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("adult")
    val adult: Boolean = false,
    @SerialName("images")
    val images: List<LinkDraftImageDto> = emptyList(),
    @SerialName("media")
    val media: MediaDto? = null,
)

@Serializable
data class LinkDraftImageDto(
    @SerialName("url")
    val url: String,
    @SerialName("selected")
    val selected: Boolean = false,
)
