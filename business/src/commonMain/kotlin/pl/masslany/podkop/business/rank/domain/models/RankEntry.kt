package pl.masslany.podkop.business.rank.domain.models

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Rank

data class RankEntry(
    val username: String,
    val avatarUrl: String,
    val gender: Gender,
    val color: NameColor,
    val memberSince: LocalDateTime?,
    val summary: RankSummary,
    val rank: Rank,
)
