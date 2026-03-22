package pl.masslany.podkop.features.blacklists.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState

data class BlacklistedUserSuggestionItemState(
    val username: String,
    val avatarState: AvatarState,
    val nameColorType: NameColorType,
) : BlacklistSuggestionItemState {
    override val key: String = "user:$username"
}
