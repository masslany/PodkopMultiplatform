package pl.masslany.podkop.features.rank

import pl.masslany.podkop.business.rank.domain.models.RankEntry
import pl.masslany.podkop.common.extensions.toMemberSinceState
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType

internal fun RankEntry.toRankUserItemState(): RankUserItemState {
    return RankUserItemState(
        username = username,
        avatarUrl = avatarUrl,
        genderIndicatorType = gender.toGenderIndicatorType(),
        nameColorType = color.toNameColorType(),
        memberSinceState = memberSince.toMemberSinceState(),
        position = rank.position,
        trend = rank.trend,
        actionsCount = summary.actions,
        linksCount = summary.links,
        entriesCount = summary.entries,
        followersCount = summary.followers,
    )
}
