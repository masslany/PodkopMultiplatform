package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.domain.models.common.Voted

fun Int?.toVoted(): Voted {
    return when (this) {
        -1 -> Voted.Negative
        1 -> Voted.Positive
        else -> Voted.None
    }
}
