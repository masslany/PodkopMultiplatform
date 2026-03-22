package pl.masslany.podkop.features.blacklists.preview

import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.blacklists.BlacklistsActions
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpBlacklistsActions : BlacklistsActions, TopBarActions by NoOpTopBarActions {
    override fun onCategorySelected(category: BlacklistCategoryType) = Unit

    override fun onRefresh() = Unit

    override fun onAddInputChanged(value: String) = Unit

    override fun onAddClicked() = Unit

    override fun onSuggestionClicked(value: String) = Unit

    override fun onRetrySuggestionsClicked() = Unit

    override fun onEntryClicked(item: BlacklistEntryState) = Unit

    override fun onRemoveClicked(item: BlacklistEntryState) = Unit
}
