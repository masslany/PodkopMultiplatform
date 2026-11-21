package pl.masslany.podkop.features.privatemessages.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UserSuggestionsState(
    val status: UserSuggestionsStatus,
    val items: ImmutableList<PrivateMessageUserSuggestionItemState>,
) {
    companion object {
        val initial = UserSuggestionsState(
            status = UserSuggestionsStatus.Hidden,
            items = persistentListOf(),
        )
    }
}
