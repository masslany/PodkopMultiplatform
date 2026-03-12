package pl.masslany.podkop.business.links.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDraftPublishRequestDto(
    @SerialName("data")
    val data: LinkDraftPublishDataDto,
)

@Serializable
data class LinkDraftPublishDataDto(
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
    @SerialName("embed")
    val embed: LinkDraftPublishEmbedDto = LinkDraftPublishEmbedDto(),
)

@Serializable
data class LinkDraftPublishEmbedDto(
    @SerialName("age_category")
    val ageCategory: String = AGE_CATEGORY_ALL,
    @SerialName("accept_media_embed_claim")
    val acceptMediaEmbedClaim: Boolean = true,
    @SerialName("commercial")
    val commercial: Boolean = false,
) {
    private companion object {
        const val AGE_CATEGORY_ALL = "all"
    }
}
