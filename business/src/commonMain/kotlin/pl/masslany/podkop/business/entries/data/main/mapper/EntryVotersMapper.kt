package pl.masslany.podkop.business.entries.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.network.models.common.UserDto
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.business.entries.domain.models.EntryVoter
import pl.masslany.podkop.business.entries.domain.models.EntryVoters

fun EntryVotersResponseDto.toEntryVoters(): EntryVoters {
    return EntryVoters(
        data = data.map { it.toEntryVoter() },
        pagination = pagination?.toPagination(),
    )
}

private fun UserDto.toEntryVoter(): EntryVoter {
    return EntryVoter(
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
