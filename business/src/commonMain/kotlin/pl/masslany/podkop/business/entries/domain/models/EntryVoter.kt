package pl.masslany.podkop.business.entries.domain.models

import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class EntryVoter(
    val username: String,
    val avatar: String,
    val gender: Gender,
    val color: NameColor,
    val online: Boolean,
    val company: Boolean,
    val verified: Boolean,
    val status: String,
)
