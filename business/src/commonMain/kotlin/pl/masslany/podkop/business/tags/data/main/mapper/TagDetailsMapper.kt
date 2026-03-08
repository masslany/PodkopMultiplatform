package pl.masslany.podkop.business.tags.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toMedia
import pl.masslany.podkop.business.tags.data.network.models.TagDetailsDto
import pl.masslany.podkop.business.tags.domain.models.TagDetails

fun TagDetailsDto.toTagDetails(): TagDetails {
    return TagDetails(
        name = name.orEmpty(),
        description = description.orEmpty(),
        followers = followers ?: 0,
        media = media?.toMedia(),
        isObserved = follow ?: false,
        areNotificationsEnabled = notifications ?: false,
        isBlacklisted = blacklist ?: false,
    )
}
