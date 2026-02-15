package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.profile.data.network.models.SummaryDto
import pl.masslany.podkop.business.profile.domain.models.Summary

fun SummaryDto.toSummary(): Summary {
    return Summary(
        actions = actions,
        entries = entries,
        links = links,
        followers = followers,
        followingTags = followingTags,
        followingUsers = followingUsers,
    )
}
