package pl.masslany.podkop.features.blacklists.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BlacklistCategoryState(
    val type: BlacklistCategoryType,
    val totalCount: Int,
    val isLoading: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val items: ImmutableList<BlacklistEntryState>,
    val addInput: String,
    val isActionsInProgress: Boolean,
    val canSubmit: Boolean,
    val suggestions: BlacklistSuggestionsState,
) {
    companion object {
        fun initial(type: BlacklistCategoryType) = BlacklistCategoryState(
            type = type,
            totalCount = 0,
            isLoading = true,
            isError = false,
            isPaginating = false,
            items = persistentListOf(),
            addInput = "",
            isActionsInProgress = false,
            canSubmit = false,
            suggestions = BlacklistSuggestionsState.initial,
        )
    }
}
