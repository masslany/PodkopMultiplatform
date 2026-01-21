package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.VotesDto
import pl.masslany.podkop.business.common.domain.models.common.Votes


fun VotesDto.toVotes(): Votes {
    return Votes(
        count = this.count ?: 0,
        down = this.down,
        up = this.up,
    )
}
