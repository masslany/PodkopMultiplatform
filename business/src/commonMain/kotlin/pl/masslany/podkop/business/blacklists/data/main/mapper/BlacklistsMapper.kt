package pl.masslany.podkop.business.blacklists.data.main.mapper

import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUserDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUsersResponseDto
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomain
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomains
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTag
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTags
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUser
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUsers
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination

fun BlacklistedUsersResponseDto.toBlacklistedUsers(): BlacklistedUsers {
    return BlacklistedUsers(
        data = data.map(BlacklistedUserDto::toBlacklistedUser),
        pagination = pagination?.toPagination(),
    )
}

fun BlacklistedTagsResponseDto.toBlacklistedTags(): BlacklistedTags {
    return BlacklistedTags(
        data = data.map(BlacklistedTagDto::toBlacklistedTag),
        pagination = pagination?.toPagination(),
    )
}

fun BlacklistedDomainsResponseDto.toBlacklistedDomains(): BlacklistedDomains {
    return BlacklistedDomains(
        data = data.map(BlacklistedDomainDto::toBlacklistedDomain),
        pagination = pagination?.toPagination(),
    )
}

private fun BlacklistedUserDto.toBlacklistedUser(): BlacklistedUser {
    return BlacklistedUser(
        username = username,
        createdAt = createdAt,
        gender = gender.toGender(),
        color = color.toNameColor(),
        avatarUrl = avatar,
    )
}

private fun BlacklistedTagDto.toBlacklistedTag(): BlacklistedTag {
    return BlacklistedTag(
        name = name,
        createdAt = createdAt,
    )
}

private fun BlacklistedDomainDto.toBlacklistedDomain(): BlacklistedDomain {
    return BlacklistedDomain(
        domain = domain,
        createdAt = createdAt,
    )
}
