package pl.masslany.podkop.business.rank.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.rank.data.network.models.RankEntryDto
import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto
import pl.masslany.podkop.business.rank.data.network.models.RankSummaryDto
import pl.masslany.podkop.business.rank.domain.models.RankEntries
import pl.masslany.podkop.business.rank.domain.models.RankEntry
import pl.masslany.podkop.business.rank.domain.models.RankSummary

fun RankResponseDto.toRankEntries(): RankEntries {
    return RankEntries(
        data = data.map { it.toRankEntry() },
        pagination = pagination?.toPagination(),
    )
}

fun RankEntryDto.toRankEntry(): RankEntry {
    return RankEntry(
        username = username,
        avatarUrl = avatar,
        gender = gender.toGender(),
        color = color.toNameColor(),
        memberSince = memberSince,
        summary = summary.toRankSummary(),
        rank = Rank(
            position = rank.position ?: 0,
            trend = rank.trend,
        ),
    )
}

fun RankSummaryDto.toRankSummary(): RankSummary {
    return RankSummary(
        actions = actions,
        links = links,
        entries = entries,
        followers = followers,
    )
}
