package pl.masslany.podkop.common.models

import androidx.annotation.StringRes
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
    @StringRes val voteReason: Int? = null,
)
