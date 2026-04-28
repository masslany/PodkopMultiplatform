package pl.masslany.podkop.features.privatemessages.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.requireNumber
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.NewConversationScreen
import pl.masslany.podkop.features.privatemessages.models.PrivateMessagesScreenState
import pl.masslany.podkop.features.privatemessages.models.toInboxConversationItemStates

class PrivateMessagesViewModel(
    private val authRepository: AuthRepository,
    private val privateMessagesRepository: PrivateMessagesRepository,
    private val privateMessagesBackgroundNotificationsController: PrivateMessagesBackgroundNotificationsController,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {
    private val conversations = MutableStateFlow(persistentListOf<PrivateMessageConversation>())
    private val _state = MutableStateFlow(PrivateMessagesScreenState.initial)

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { newItems ->
            conversations.update { previousItems ->
                appendDistinctByUsername(previousItems, newItems).toPersistentList()
            }
        },
        onError = {
            logger.error("Failed to load paginated private messages conversations", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        privateMessagesRepository.getConversations(
            page = request.requireNumber(),
        )
    }

    val state = combine(
        _state,
        conversations,
        paginator.state,
    ) { currentState, items, paginatorState ->
        currentState.copy(
            conversations = items.toInboxConversationItemStates().toPersistentList(),
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = PrivateMessagesScreenState.initial,
    )

    init {
        loadData(showLoading = true)
        onScreenOpened()
    }

    fun onTopBarBackClicked() {
        appNavigator.back()
    }

    fun onConversationClicked(username: String) {
        appNavigator.navigateTo(ConversationScreen(username = username))
    }

    fun onNewConversationClicked() {
        appNavigator.navigateTo(NewConversationScreen)
    }

    fun onRefresh() {
        loadData(showLoading = false)
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _state.update {
            it.copy(shouldRequestNotificationPermission = false)
        }
        if (!granted) return

        viewModelScope.launch {
            privateMessagesBackgroundNotificationsController.onNotificationPermissionGranted()
        }
    }

    fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean =
        paginator.shouldPaginate(lastVisibleIndex, totalItems)

    fun paginate() {
        paginator.paginate()
    }

    private fun onScreenOpened() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _state.update {
                    it.copy(shouldRequestNotificationPermission = false)
                }
                return@launch
            }

            if (privateMessagesBackgroundNotificationsController.areSystemNotificationsEnabled()) {
                _state.update {
                    it.copy(shouldRequestNotificationPermission = false)
                }
                privateMessagesBackgroundNotificationsController.onNotificationPermissionGranted()
            } else {
                _state.update {
                    it.copy(shouldRequestNotificationPermission = true)
                }
            }
        }
    }

    private fun loadData(showLoading: Boolean) {
        _state.update { previous ->
            previous.copy(
                isLoading = showLoading,
                isRefreshing = !showLoading,
                isError = false,
            )
        }

        viewModelScope.launch {
            privateMessagesRepository.getConversations(page = 1)
                .onSuccess { page ->
                    conversations.value = page.data.toPersistentList()
                    paginator.setup(page.pagination, page.data.size)
                    _state.update { previous ->
                        previous.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = false,
                        )
                    }
                }
                .onFailure {
                    logger.error("Failed to load private message conversations", it)
                    _state.update { previous ->
                        previous.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = conversations.value.isEmpty(),
                        )
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }
}

private fun appendDistinctByUsername(
    current: List<PrivateMessageConversation>,
    incoming: List<PrivateMessageConversation>,
): List<PrivateMessageConversation> {
    val knownUsernames = current.mapTo(mutableSetOf()) { it.username }
    return buildList(current.size + incoming.size) {
        addAll(current)
        incoming.forEach { conversation ->
            if (knownUsernames.add(conversation.username)) {
                add(conversation)
            }
        }
    }
}
