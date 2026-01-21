package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.RankDto
import pl.masslany.podkop.business.common.domain.models.common.Rank

fun RankDto.toRank(): Rank {
    return Rank(
        position = this.position ?: 0,
        trend = this.trend,
    )
}
