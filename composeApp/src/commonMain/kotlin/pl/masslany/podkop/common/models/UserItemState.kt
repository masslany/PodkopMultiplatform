package pl.masslany.podkop.common.models

import org.jetbrains.compose.resources.StringResource
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType

data class UserItemState(
    val username: String,
    val avatarUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
    val online: Boolean,
    val company: Boolean,
    val verified: Boolean,
    val status: String,
    val voteReason: StringResource? = null,
)
