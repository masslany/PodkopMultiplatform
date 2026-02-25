package pl.masslany.podkop.features.search

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType

data class SearchScreenState(
    val query: String,
    val minQueryLength: Int,
    val isSearching: Boolean,
    val showUserSuggestions: Boolean,
    val tags: SearchSectionState<TagSuggestionItemState>,
    val users: SearchSectionState<UserSuggestionItemState>,
) {
    companion object {
        val initial = SearchScreenState(
            query = "",
            minQueryLength = 3,
            isSearching = false,
            showUserSuggestions = false,
            tags = SearchSectionState(),
            users = SearchSectionState(),
        )
    }
}

data class SearchSectionState<T>(
    val status: SearchSectionStatus = SearchSectionStatus.Hidden,
    val items: List<T> = emptyList(),
)

enum class SearchSectionStatus {
    Hidden,
    Loading,
    Content,
    Error,
}

data class TagSuggestionItemState(val name: String, val followers: Int)

data class UserSuggestionItemState(
    val username: String,
    val avatarUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
)
