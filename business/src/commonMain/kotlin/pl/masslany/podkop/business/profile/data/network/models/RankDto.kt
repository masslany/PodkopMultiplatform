package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RankDto(
    @SerialName("position")
    val position: Int?,
    @SerialName("trend")
    val trend: Int,
)
