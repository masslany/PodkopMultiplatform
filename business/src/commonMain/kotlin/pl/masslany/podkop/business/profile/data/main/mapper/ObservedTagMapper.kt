package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.profile.data.network.models.ObservedTagDto
import pl.masslany.podkop.business.profile.domain.models.ObservedTag

fun ObservedTagDto.toObservedTag(): ObservedTag {
    return ObservedTag(
        name = name,
        pinned = pinned,
    )
}
