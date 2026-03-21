package pl.masslany.podkop.features.privatemessages.conversation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.PrivateMessageComposerController
import pl.masslany.podkop.features.privatemessages.models.ConversationScreenState
import pl.masslany.podkop.features.privatemessages.models.mergePrivateConversationMessages
import pl.masslany.podkop.features.privatemessages.models.toConversationMessages
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class ConversationViewModel(
    private val screen: ConversationScreen,
    private val privateMessagesRepository: PrivateMessagesRepository,
    private val notificationsRepository: NotificationsRepository,
    private val mediaRepository: MediaRepository,
    private val appNavigator: AppNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val topBarActions: TopBarActions,
) : ViewModel(),
    ConversationActions,
    TopBarActions by topBarActions {
    private val rawMessages = MutableStateFlow(persistentListOf<PrivateMessage>())
    private val composerController = PrivateMessageComposerController(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        mediaRepository = mediaRepository,
        appNavigator = appNavigator,
        logger = logger,
        snackbarManager = snackbarManager,
        keyPrefix = "conversation_${screen.username}_",
    )
    private val _state = MutableStateFlow(
        ConversationScreenState.initial(username = screen.username).copy(
            composer = composerController.state.value,
        ),
    )
    private var hasLoadedOnce = false
    private var pollingJob: Job? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { olderMessages ->
            rawMessages.update { current ->
                mergePrivateConversationMessages(current, olderMessages).toPersistentList()
            }
        },
        onError = {
            logger.error("Failed to load older private message thread items for ${screen.username}", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        privateMessagesRepository.getConversationMessages(
            username = screen.username,
            page = when (request) {
                is PageRequest.Cursor -> request.key
                is PageRequest.Index -> request.page
            },
        )
    }

    val state = combine(
        _state,
        rawMessages,
        composerController.state,
        paginator.state,
    ) { currentState, messages, composer, paginatorState ->
        currentState.copy(
            messages = messages.toConversationMessages().toPersistentList(),
            composer = composer,
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = ConversationScreenState.initial(username = screen.username),
    )

    init {
        appNavigator.registerBackHandler(screen) {
            onSystemBack()
        }
    }

    fun onScreenStarted() {
        markThreadAsRead()
        if (!hasLoadedOnce || (state.value.isError && rawMessages.value.isEmpty())) {
            loadThread(showLoading = true, showRefreshing = false, scrollToLatest = true)
        } else {
            refreshNewerMessages()
            startPolling()
        }
    }

    fun onScreenStopped() {
        stopPolling()
    }

    override fun onTopBarBackClicked() {
        requestClose()
    }

    override fun onRetryClicked() {
        loadThread(showLoading = true, showRefreshing = false, scrollToLatest = true)
    }

    override fun onRefresh() {
        loadThread(
            showLoading = false,
            showRefreshing = true,
            scrollToLatest = rawMessages.value.isEmpty(),
        )
    }

    override fun onComposerTextChanged(content: TextFieldValue) {
        composerController.onTextChanged(content)
    }

    override fun onComposerAdultChanged(adult: Boolean) {
        composerController.onAdultChanged(adult)
    }

    override fun onComposerPhotoAttachClicked() {
        composerController.onPhotoAttachClicked()
    }

    override fun onComposerPhotoRemoved() {
        composerController.onPhotoRemoved()
    }

    override fun onComposerSubmit() {
        composerController.submit(
            submitAction = { payload ->
                privateMessagesRepository.openConversation(
                    username = screen.username,
                    content = payload.content,
                    adult = payload.adult,
                    photoKey = payload.photoKey,
                    embed = null,
                )
            },
            onSuccess = { message ->
                rawMessages.update { current ->
                    mergePrivateConversationMessages(current, listOf(message)).toPersistentList()
                }
                _state.update { previous ->
                    previous.copy(scrollToLatestMessage = previous.scrollToLatestMessage + 1)
                }
            },
        )
    }

    override fun onProfileClicked(username: String) {
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onTagClicked(tag: String) {
        appNavigator.navigateTo(TagScreen(tag = tag))
    }

    override fun onUrlClicked(url: String) {
        appNavigator.openExternalLink(url)
    }

    override fun onImageClicked(url: String) {
        appNavigator.navigateTo(ImageViewerScreen(imageUrl = url))
    }

    fun shouldPaginate(firstVisibleItemIndex: Int?, canScrollForward: Boolean): Boolean {
        if (_state.value.isLoading || _state.value.isRefreshing) {
            return false
        }
        if (paginator.state.value != PaginatorState.Idle) {
            return false
        }
        if (!canScrollForward) {
            return false
        }
        return firstVisibleItemIndex != null && firstVisibleItemIndex <= TOP_PAGINATION_THRESHOLD
    }

    fun paginate() {
        paginator.paginate()
    }

    override fun onCleared() {
        stopPolling()
        appNavigator.unregisterBackHandler(screen)
        composerController.onCleared()
        super.onCleared()
    }

    private fun requestClose() {
        composerController.requestClose {
            stopPolling()
            appNavigator.unregisterBackHandler(screen)
            appNavigator.back()
        }
    }

    private fun onSystemBack(): Boolean {
        if (composerController.shouldInterceptBack()) {
            requestClose()
            return true
        }

        stopPolling()
        appNavigator.unregisterBackHandler(screen)
        return false
    }

    private fun loadThread(
        showLoading: Boolean,
        showRefreshing: Boolean,
        scrollToLatest: Boolean,
    ) {
        _state.update { previous ->
            previous.copy(
                isLoading = showLoading,
                isRefreshing = showRefreshing,
                isError = false,
            )
        }

        viewModelScope.launch {
            privateMessagesRepository.getConversationMessages(username = screen.username, page = 1)
                .onSuccess { page ->
                    hasLoadedOnce = true
                    rawMessages.value = mergePrivateConversationMessages(emptyList(), page.data).toPersistentList()
                    paginator.setup(page.pagination, page.data.size)
                    _state.update { previous ->
                        previous.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = false,
                            scrollToLatestMessage = if (scrollToLatest && page.data.isNotEmpty()) {
                                previous.scrollToLatestMessage + 1
                            } else {
                                previous.scrollToLatestMessage
                            },
                        )
                    }
                    startPolling()
                }
                .onFailure {
                    logger.error("Failed to load private message thread for ${screen.username}", it)
                    _state.update { previous ->
                        previous.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = rawMessages.value.isEmpty(),
                        )
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun refreshNewerMessages() {
        if (!hasLoadedOnce || _state.value.isLoading || _state.value.isRefreshing) {
            return
        }

        viewModelScope.launch {
            privateMessagesRepository.getConversationMessagesNewer(username = screen.username)
                .onSuccess { page ->
                    val merged = mergePrivateConversationMessages(rawMessages.value, page.data)
                    if (merged != rawMessages.value) {
                        rawMessages.value = merged.toPersistentList()
                    }
                }
                .onFailure {
                    logger.warn("Failed to poll newer private messages for ${screen.username}", it)
                }
        }
    }

    private fun markThreadAsRead() {
        viewModelScope.launch {
            privateMessagesRepository.readAll()
                .onSuccess {
                    notificationsRepository.refreshStatus()
                        .onFailure { error ->
                            logger.warn("Failed to refresh notifications after marking PM thread as read", error)
                        }
                }
                .onFailure {
                    logger.warn("Failed to mark private messages as read", it)
                }
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) {
            return
        }

        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLLING_INTERVAL)
                refreshNewerMessages()
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private companion object {
        const val TOP_PAGINATION_THRESHOLD = 2
        val POLLING_INTERVAL = 30.seconds
    }
}
