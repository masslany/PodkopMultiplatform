package pl.masslany.podkop.features.blacklists.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BlacklistSuggestionsState(
    val status: BlacklistSuggestionsStatus,
    val items: ImmutableList<BlacklistSuggestionItemState>,
) {
    companion object {
        val initial = BlacklistSuggestionsState(
            status = BlacklistSuggestionsStatus.Hidden,
            items = persistentListOf(),
        )
    }
}
