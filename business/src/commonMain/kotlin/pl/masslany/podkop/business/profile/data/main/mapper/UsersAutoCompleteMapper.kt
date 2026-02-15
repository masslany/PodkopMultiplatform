package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto
import pl.masslany.podkop.business.profile.domain.models.UserAutoCompleteItem
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete

fun UsersAutoCompleteResponseDto.toUsersAutoComplete(): UsersAutoComplete {
    return UsersAutoComplete(
        users = data.map {
            UserAutoCompleteItem(
                username = it.username,
                avatarUrl = it.avatar,
                gender = it.gender.toGender(),
                color = it.color.toNameColor(),
            )
        },
    )
}
