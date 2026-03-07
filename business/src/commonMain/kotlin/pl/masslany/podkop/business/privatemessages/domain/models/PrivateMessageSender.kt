package pl.masslany.podkop.business.privatemessages.domain.models

import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class PrivateMessageSender(
    val username: String,
    val avatarUrl: String?,
    val gender: Gender,
    val nameColor: NameColor,
)
