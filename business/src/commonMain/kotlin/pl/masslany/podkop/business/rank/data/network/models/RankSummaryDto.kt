package pl.masslany.podkop.business.rank.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RankSummaryDto(
    @SerialName("actions")
    val actions: Int,
    @SerialName("entries")
    val entries: Int,
    @SerialName("followers")
    val followers: Int,
    @SerialName("links")
    val links: Int,
)
