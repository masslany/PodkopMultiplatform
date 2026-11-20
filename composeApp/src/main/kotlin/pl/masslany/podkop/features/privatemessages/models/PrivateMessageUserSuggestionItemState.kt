package pl.masslany.podkop.features.privatemessages.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState

data class PrivateMessageUserSuggestionItemState(
    val username: String,
    val avatarState: AvatarState,
    val nameColorType: NameColorType,
)
