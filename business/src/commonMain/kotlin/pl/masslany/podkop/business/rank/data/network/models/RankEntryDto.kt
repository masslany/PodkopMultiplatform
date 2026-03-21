package pl.masslany.podkop.business.rank.data.network.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer
import pl.masslany.podkop.business.common.data.network.models.common.RankDto

@Serializable
data class RankEntryDto(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("color")
    val color: String,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("member_since")
    @Serializable(with = DateAsStringSerializer::class)
    val memberSince: LocalDateTime?,
    @SerialName("rank")
    val rank: RankDto,
    @SerialName("summary")
    val summary: RankSummaryDto,
    @SerialName("username")
    val username: String,
)
