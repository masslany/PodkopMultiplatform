package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.data.network.models.common.UserDto
import pl.masslany.podkop.business.profile.domain.models.ObservedUser

fun UserDto.toObservedUser(): ObservedUser {
    return ObservedUser(
        username = username,
        avatar = avatar,
        gender = gender.toGender(),
        color = color.toNameColor(),
        online = online,
        company = company,
        verified = verified,
        status = status,
    )
}
