package pl.masslany.podkop.business.common.data.network.models.comments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    @SerialName("filter")
    val filter: String,
    @SerialName("page")
    val page: Int,
    @SerialName("parent_page")
    val parentPage: Int? = null,
)
