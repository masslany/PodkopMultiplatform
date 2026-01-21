package pl.masslany.podkop.business.common.data.network.models.comments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.AuthorDto
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable
data class ParentDto(
    @SerialName("author")
    val author: AuthorDto,
    @SerialName("id")
    val id: Int,
    @SerialName("link")
    val link: ResourceItemDto? = null,
    @SerialName("location")
    val location: List<LocationDto>,
    @SerialName("resource")
    val resource: String,
    @SerialName("slug")
    val slug: String,
)
