package pl.masslany.podkop.business.entries.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.data.network.models.common.UserDto
import pl.masslany.podkop.business.common.domain.models.common.Voter
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto

fun EntryVotersResponseDto.toVoters(): Voters {
    return Voters(
        data = data.map { it.toVoter() },
        pagination = pagination?.toPagination(),
    )
}

private fun UserDto.toVoter(): Voter {
    return Voter(
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
