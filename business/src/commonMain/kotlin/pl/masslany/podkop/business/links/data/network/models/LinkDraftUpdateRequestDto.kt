package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDraftUpdateRequestDto(
    @SerialName("data")
    val data: LinkDraftUpdateDataDto,
)

@Serializable
data class LinkDraftUpdateDataDto(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("tags")
    val tags: List<String>,
    @SerialName("photo")
    val photo: String? = null,
    @SerialName("adult")
    val adult: Boolean,
    @SerialName("selected_image")
    val selectedImage: Int? = null,
)
