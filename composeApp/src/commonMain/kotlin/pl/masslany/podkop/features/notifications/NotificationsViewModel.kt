package pl.masslany.podkop.features.notifications

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
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.notifications.models.NotificationGroupChipState
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    NotificationsActions,
    TopBarActions by topBarActions {

    private val items = MutableStateFlow(persistentListOf<NotificationItem>())
    private val _state = MutableStateFlow(NotificationsScreenState.initial)

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            items.update { previousItems ->
                (previousItems + data).toPersistentList()
            }
        },
        onError = {
            logger.error(
                message = "Failed to load paginated notifications for group=${_state.value.selectedGroup}",
                throwable = it,
            )
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        notificationsRepository.getNotifications(
            group = _state.value.selectedGroup,
            page = when (request) {
                is PageRequest.Cursor -> request.key
                is PageRequest.Index -> request.page
            },
        )
    }

    val state = combine(
        _state,
        items,
        notificationsRepository.status,
        paginator.state,
    ) { currentState, items, status, paginatorState ->
        currentState.copy(
            groups = NotificationGroup.entries
                .map { group ->
                    NotificationGroupChipState(
                        group = group,
                        unreadCount = status.unreadCount(group),
                        selected = group == currentState.selectedGroup,
                    )
                }
                .toPersistentList(),
            items = items
                .toNotificationItemStates(currentState.selectedGroup)
                .toPersistentList(),
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = NotificationsScreenState.initial,
    )

    init {
        fetchNotifications(refreshStatus = true)
    }

    fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean =
        paginator.shouldPaginate(lastVisibleIndex, totalItems)

    fun paginate() {
        paginator.paginate()
    }

    override fun onGroupSelected(group: NotificationGroup) {
        if (group == state.value.selectedGroup && !state.value.isError) return

        _state.update { previousState ->
            previousState.copy(
                selectedGroup = group,
                isLoading = true,
                isRefreshing = false,
                isError = false,
            )
        }
        items.value = persistentListOf()
        fetchNotifications(refreshStatus = false)
    }

    override fun onNotificationClicked(id: String) {
        val item = state.value.items.firstOrNull { notification -> notification.id == id } ?: return
        val selectedGroup = state.value.selectedGroup
        navigateTo(item.navigationTarget)

        if (item.isRead || item.notificationIds.size != 1) return

        viewModelScope.launch {
            notificationsRepository.markAsRead(
                group = selectedGroup,
                id = item.notificationIds.firstOrNull() ?: return@launch,
            )
                .onSuccess {
                    items.update { currentItems ->
                        currentItems
                            .map { currentItem ->
                                if (currentItem.id in item.notificationIds) {
                                    currentItem.copy(isRead = true)
                                } else {
                                    currentItem
                                }
                            }
                            .toPersistentList()
                    }
                }
                .onFailure {
                    logger.warn(
                        message = "Failed to mark notification $id as read in group=$selectedGroup",
                        throwable = it,
                    )
                }
        }
    }

    override fun onRefresh() {
        _state.update { previousState ->
            previousState.copy(
                isRefreshing = true,
                isError = false,
            )
        }
        fetchNotifications(refreshStatus = true)
    }

    override fun onMarkAllAsReadClicked() {
        val selectedGroup = state.value.selectedGroup
        if (state.value.isMarkingAllAsRead || !state.value.canMarkAllAsRead) return

        viewModelScope.launch {
            _state.update { previousState ->
                previousState.copy(isMarkingAllAsRead = true)
            }

            notificationsRepository.markAllAsRead(selectedGroup)
                .onSuccess {
                    items.update { currentItems ->
                        currentItems
                            .map { item -> item.copy(isRead = true) }
                            .toPersistentList()
                    }
                }
                .onFailure {
                    logger.error(
                        message = "Failed to mark all notifications as read for group=$selectedGroup",
                        throwable = it,
                    )
                    snackbarManager.tryEmitGenericError()
                }

            _state.update { previousState ->
                previousState.copy(isMarkingAllAsRead = false)
            }
        }
    }

    private fun fetchNotifications(refreshStatus: Boolean) {
        val selectedGroup = _state.value.selectedGroup

        viewModelScope.launch {
            if (refreshStatus) {
                notificationsRepository.refreshStatus()
                    .onFailure {
                        logger.warn(
                            message = "Failed to refresh notifications status for group=$selectedGroup",
                            throwable = it,
                        )
                    }
            }

            notificationsRepository.getNotifications(group = selectedGroup, page = 1)
                .onSuccess { page ->
                    items.value = page.data.toPersistentList()
                    paginator.setup(page.pagination, page.data.size)
                    _state.update { previousState ->
                        previousState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = false,
                        )
                    }
                }
                .onFailure {
                    logger.error(
                        message = "Failed to load notifications for group=$selectedGroup",
                        throwable = it,
                    )
                    val shouldShowErrorScreen = state.value.items.isEmpty()
                    _state.update { previousState ->
                        previousState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = shouldShowErrorScreen,
                        )
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun navigateTo(target: NotificationNavigationTarget) {
        when (target) {
            is NotificationNavigationTarget.Conversation -> {
                appNavigator.navigateTo(ConversationScreen(username = target.username))
            }

            is NotificationNavigationTarget.Entry -> {
                appNavigator.navigateTo(EntryDetailsScreen.forEntry(id = target.id))
            }

            is NotificationNavigationTarget.External -> {
                appNavigator.openExternalLink(target.url)
            }

            is NotificationNavigationTarget.Link -> {
                appNavigator.navigateTo(LinkDetailsScreen(id = target.id))
            }

            NotificationNavigationTarget.None -> Unit

            is NotificationNavigationTarget.Profile -> {
                appNavigator.navigateTo(ProfileScreen(username = target.username))
            }

            is NotificationNavigationTarget.Tag -> {
                appNavigator.navigateTo(TagScreen(tag = target.name))
            }
        }
    }
}
