package pl.masslany.podkop.features.search.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.search.SearchScreenState
import pl.masslany.podkop.features.search.SearchSectionState
import pl.masslany.podkop.features.search.SearchSectionStatus
import pl.masslany.podkop.features.search.TagSuggestionItemState
import pl.masslany.podkop.features.search.UserSuggestionItemState

class SearchScreenStateProvider : PreviewParameterProvider<SearchScreenState> {
    override val values: Sequence<SearchScreenState> = sequenceOf(
        SearchScreenState.initial,
        SearchScreenState.initial.copy(query = "ko"),
        SearchScreenState.initial.copy(
            query = "kotl",
            showUserSuggestions = true,
            isSearching = true,
            tags = SearchSectionState(status = SearchSectionStatus.Loading),
            users = SearchSectionState(status = SearchSectionStatus.Loading),
        ),
        SearchScreenState.initial.copy(
            query = "kotl",
            showUserSuggestions = true,
            tags = SearchSectionState(
                status = SearchSectionStatus.Content,
                items = listOf(
                    TagSuggestionItemState(name = "kotlin", followers = 2100),
                    TagSuggestionItemState(name = "koty", followers = 1432),
                ),
            ),
            users = SearchSectionState(
                status = SearchSectionStatus.Content,
                items = listOf(
                    UserSuggestionItemState(
                        username = "kotlin",
                        avatarUrl = "https://picsum.photos/seed/search-user-1/96/96",
                        genderIndicatorType = GenderIndicatorType.Male,
                        nameColorType = NameColorType.Orange,
                    ),
                    UserSuggestionItemState(
                        username = "kotlubigryzc",
                        avatarUrl = "https://picsum.photos/seed/search-user-2/96/96",
                        genderIndicatorType = GenderIndicatorType.Female,
                        nameColorType = NameColorType.Green,
                    ),
                ),
            ),
        ),
        SearchScreenState.initial.copy(
            query = "kotl",
            showUserSuggestions = true,
            tags = SearchSectionState(status = SearchSectionStatus.Error),
            users = SearchSectionState(status = SearchSectionStatus.Content, items = emptyList()),
        ),
    )
}
