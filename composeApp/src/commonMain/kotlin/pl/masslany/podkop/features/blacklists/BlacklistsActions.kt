package pl.masslany.podkop.features.blacklists

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface BlacklistsActions : TopBarActions {
    fun onCategorySelected(category: BlacklistCategoryType)

    fun onRefresh()

    fun onAddInputChanged(value: String)

    fun onAddClicked()

    fun onSuggestionClicked(value: String)

    fun onRetrySuggestionsClicked()

    fun onEntryClicked(item: BlacklistEntryState)

    fun onRemoveClicked(item: BlacklistEntryState)
}
