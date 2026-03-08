package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.domain.models.Profile

fun ProfileDto.toProfile(): Profile {
    return Profile(
        name = data.username,
        avatarUrl = data.avatar,
        gender = data.gender.toGender(),
        color = data.color.toNameColor(),
        backgroundUrl = data.background,
        summary = data.summary.toSummary(),
        memberSince = data.memberSince,
        isObserved = data.follow,
        isBlacklisted = data.blacklist,
        canManageObservation = data.actions.follow,
    )
}
