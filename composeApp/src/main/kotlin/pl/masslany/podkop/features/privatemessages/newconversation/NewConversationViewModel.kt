package pl.masslany.podkop.features.privatemessages.newconversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.models.NewConversationScreenState
import pl.masslany.podkop.features.privatemessages.models.PrivateMessageUserSuggestionItemState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsStatus
import pl.masslany.podkop.features.privatemessages.models.normalizePrivateMessageUsername

class NewConversationViewModel(
    private val profileRepository: ProfileRepository,
    private val appNavigator: AppNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val logger: AppLogger,
) : ViewModel() {
    private val restoredUsername = savedStateHandle.get<String>(STATE_USERNAME).orEmpty()
    private val usernameChanges = savedStateHandle.getStateFlow(STATE_USERNAME, restoredUsername)
    private val suggestions = MutableStateFlow(UserSuggestionsState.initial)

    val state = combine(
        usernameChanges,
        suggestions,
    ) { username, suggestionsState ->
        NewConversationScreenState(
            username = username,
            suggestions = suggestionsState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = NewConversationScreenState.initial.copy(username = restoredUsername),
    )

    init {
        observeUsernameChanges()
    }

    fun onTopBarBackClicked() {
        appNavigator.back()
    }

    fun onUsernameChanged(value: String) {
        savedStateHandle[STATE_USERNAME] = value
    }

    fun onSuggestionClicked(username: String) {
        clearUsernameState()
        appNavigator.back()
        appNavigator.navigateTo(ConversationScreen(username = username))
    }

    fun onRetrySuggestionsClicked() {
        viewModelScope.launch {
            loadSuggestions(query = state.value.username)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeUsernameChanges() {
        viewModelScope.launch {
            usernameChanges
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collect { query ->
                    loadSuggestions(query = query)
                }
        }
    }

    private suspend fun loadSuggestions(query: String) {
        val normalizedQuery = query.normalizePrivateMessageUsername()
        if (normalizedQuery.length < MIN_USERNAME_QUERY_LENGTH) {
            return
        }

        suggestions.update { previousState ->
            previousState.copy(status = UserSuggestionsStatus.Loading)
        }
        profileRepository.getUsersAutoComplete(query = normalizedQuery)
            .onSuccess { autocomplete ->
                suggestions.update { previousState ->
                    previousState.copy(
                        status = UserSuggestionsStatus.Content,
                        items = autocomplete.users.map { user ->
                            PrivateMessageUserSuggestionItemState(
                                username = user.username,
                                avatarState = AvatarState(
                                    type = user.avatarUrl
                                        .takeIf(String::isNotBlank)
                                        ?.let(AvatarType::NetworkImage)
                                        ?: AvatarType.NoAvatar,
                                    genderIndicatorType = user.gender.toGenderIndicatorType(),
                                ),
                                nameColorType = user.color.toNameColorType(),
                            )
                        }.toPersistentList(),
                    )
                }
            }
            .onFailure {
                logger.error("Failed to load private message user suggestions", it)
                suggestions.update { previousState ->
                    previousState.copy(status = UserSuggestionsStatus.Error)
                }
            }
    }

    private fun clearUsernameState() {
        savedStateHandle.remove<Any?>(STATE_USERNAME)
    }

    private companion object {
        const val STATE_USERNAME = "new_conversation_username"
        const val MIN_USERNAME_QUERY_LENGTH = 3
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
