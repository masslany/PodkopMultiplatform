package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationDto(
    @SerialName("per_page")
    val perPage: Int? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("total_items")
    val totalItems: Int? = null,
    @SerialName("next")
    val next: String? = null,
    @SerialName("prev")
    val prev: String? = null,
)
