package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagsResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedUsersResponseDto
import pl.masslany.podkop.business.profile.domain.models.ObservedTags
import pl.masslany.podkop.business.profile.domain.models.ObservedUsers

fun ObservedUsersResponseDto.toObservedUsers(): ObservedUsers {
    return ObservedUsers(
        data = data.map { it.toObservedUser() },
        pagination = pagination?.toPagination(),
    )
}

fun ObservedTagsResponseDto.toObservedTags(): ObservedTags {
    return ObservedTags(
        data = data.map { it.toObservedTag() },
        pagination = pagination?.toPagination(),
    )
}
