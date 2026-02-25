package pl.masslany.podkop.features.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class SearchViewModel(
    private val tagsRepository: TagsRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val savedStateHandle: SavedStateHandle,
    topBarActions: TopBarActions,
) : ViewModel(),
    SearchActions,
    TopBarActions by topBarActions {

    private val restoredQuery = savedStateHandle.get<String>(QUERY_SAVED_STATE_KEY).orEmpty()

    private val _state = MutableStateFlow(
        SearchScreenState.initial.copy(query = restoredQuery),
    )
    val state = _state.asStateFlow()

    private val queryChanges = savedStateHandle.getStateFlow(QUERY_SAVED_STATE_KEY, restoredQuery)

    init {
        observeSearchQueryChanges()
    }

    override fun onQueryChanged(value: String) {
        savedStateHandle[QUERY_SAVED_STATE_KEY] = value
        _state.update { previous ->
            previous.copy(query = value)
        }
    }

    override fun onTagClicked(tag: String) {
        appNavigator.navigateTo(TagScreen(tag = tag))
    }

    override fun onUserClicked(username: String) {
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onRetryClicked() {
        viewModelScope.launch {
            performSearch(query = state.value.query)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQueryChanges() {
        viewModelScope.launch {
            queryChanges
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query = query)
                }
        }
    }

    private suspend fun performSearch(query: String) {
        val normalizedQuery = query.trim()
        val canSearchUsers = areUserSuggestionsAvailable()

        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            _state.update { previous ->
                previous.copy(
                    minQueryLength = MIN_QUERY_LENGTH,
                    isSearching = false,
                    showUserSuggestions = canSearchUsers,
                    tags = SearchSectionState(),
                    users = SearchSectionState(),
                )
            }
            return
        }

        _state.update { previous ->
            previous.copy(
                minQueryLength = MIN_QUERY_LENGTH,
                isSearching = true,
                showUserSuggestions = canSearchUsers,
                tags = SearchSectionState(status = SearchSectionStatus.Loading),
                users = if (canSearchUsers) {
                    SearchSectionState(status = SearchSectionStatus.Loading)
                } else {
                    SearchSectionState()
                },
            )
        }

        coroutineScope {
            val tagsRequest = async {
                tagsRepository.getAutoCompleteTags(query = normalizedQuery)
            }
            val usersRequest = if (canSearchUsers) {
                async {
                    profileRepository.getUsersAutoComplete(query = normalizedQuery)
                }
            } else {
                null
            }

            val tagsState = tagsRequest.await().toTagSectionState()
            val usersState = usersRequest?.await()?.toUserSectionState() ?: SearchSectionState()

            _state.update { previous ->
                previous.copy(
                    isSearching = false,
                    showUserSuggestions = canSearchUsers,
                    tags = tagsState,
                    users = usersState,
                )
            }
        }
    }

    private suspend fun areUserSuggestionsAvailable(): Boolean = authRepository.isLoggedIn()

    private fun Result<TagsAutoComplete>.toTagSectionState(): SearchSectionState<TagSuggestionItemState> = fold(
        onSuccess = { autocomplete ->
            SearchSectionState(
                status = SearchSectionStatus.Content,
                items = autocomplete.tags.map { tag ->
                    TagSuggestionItemState(
                        name = tag.name,
                        followers = tag.observedQuantity,
                    )
                },
            )
        },
        onFailure = {
            logger.error("Failed to load tag autocomplete", it)
            SearchSectionState(status = SearchSectionStatus.Error)
        },
    )

    private fun Result<UsersAutoComplete>.toUserSectionState(): SearchSectionState<UserSuggestionItemState> = fold(
        onSuccess = { autocomplete ->
            SearchSectionState(
                status = SearchSectionStatus.Content,
                items = autocomplete.users.map { user ->
                    UserSuggestionItemState(
                        username = user.username,
                        avatarUrl = user.avatarUrl,
                        genderIndicatorType = user.gender.toGenderIndicatorType(),
                        nameColorType = user.color.toNameColorType(),
                    )
                },
            )
        },
        onFailure = {
            logger.error("Failed to load user autocomplete", it)
            SearchSectionState(status = SearchSectionStatus.Error)
        },
    )

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val QUERY_SAVED_STATE_KEY = "search_query"
    }
}
