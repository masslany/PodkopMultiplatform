package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.domain.models.ProfileShort

fun ProfileShortDto.toProfileShort(): ProfileShort {
    return ProfileShort(
        name = data.username,
        avatarUrl = data.avatar,
        gender = data.gender.toGender(),
        color = data.color.toNameColor(),
    )
}
