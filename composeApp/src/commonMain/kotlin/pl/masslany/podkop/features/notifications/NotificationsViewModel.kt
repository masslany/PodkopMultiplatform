package pl.masslany.podkop.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
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
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.notifications.models.GroupedTagContentType
import pl.masslany.podkop.features.notifications.models.NotificationGroupChipState
import pl.masslany.podkop.features.notifications.models.NotificationGroupExpansionState
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget
import pl.masslany.podkop.features.pagination.FeaturePaginationPolicies
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagContent
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

    /** Expanded grouped rows by row id. */
    private val expansions = MutableStateFlow(persistentMapOf<String, GroupExpansion>())
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
        },
    ) { request ->
        notificationsRepository.getNotifications(
            group = _state.value.selectedGroup,
            page = request,
        )
    }

    val state = combine(
        _state,
        items,
        notificationsRepository.status,
        paginator.state,
        expansions,
    ) { currentState, items, status, paginatorState, expansions ->
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
                .map { row -> expansions[row.id]?.let { row.copy(expansion = it.toState()) } ?: row }
                .toPersistentList(),
            isPaginating = paginatorState is PaginatorState.Loading,
            isPaginationError = paginatorState is PaginatorState.Error,
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

    override fun paginate() {
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
        expansions.value = persistentMapOf()
        fetchNotifications(refreshStatus = false)
    }

    override fun onNotificationClicked(id: String) {
        val item = state.value.items.firstOrNull { notification -> notification.id == id } ?: return
        val selectedGroup = state.value.selectedGroup
        navigateTo(item.navigationTarget)

        if (item.isRead) return
        if (item.groupId != null) {
            // Like on website, opening a tag's stream marks its notifications read on the server.
            (item.navigationTarget as? NotificationNavigationTarget.Tag)?.let { tag -> markTagRead(tag.name) }
            return
        }
        if (item.notificationIds.size != 1) return

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

    override fun onGroupedRowExpandToggled(id: String) {
        if (expansions.value.containsKey(id)) {
            expansions.update { current -> current.remove(id) }
            return
        }
        val groupId = state.value.items.firstOrNull { item -> item.id == id }?.groupId ?: return
        expansions.update { current -> current.put(id, GroupExpansion(groupId = groupId)) }
        loadGroupedRowPage(id)
    }

    override fun onGroupedRowShowMoreClicked(id: String) {
        loadGroupedRowPage(id)
    }

    override fun onGroupedRowNotificationClicked(
        rowId: String,
        id: String,
    ) {
        val member = expansions.value[rowId]?.members?.firstOrNull { member -> member.id == id } ?: return
        val selectedGroup = state.value.selectedGroup
        navigateTo(member.navigationTarget())
        if (member.isRead) return

        viewModelScope.launch {
            notificationsRepository.markAsRead(group = selectedGroup, id = id)
                .onSuccess { markMembersRead { notification -> notification.id == id } }
                .onFailure {
                    logger.warn(
                        message = "Failed to mark grouped notification $id as read in group=$selectedGroup",
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
                    markMembersRead { true }
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

            notificationsRepository.getNotifications(
                group = selectedGroup,
                page = selectedGroup.paginationMode().initialRequest(),
            )
                .onSuccess { page ->
                    items.value = page.data.toPersistentList()
                    expansions.value = persistentMapOf()
                    paginator.setup(
                        pagination = page.pagination,
                        initialItemCount = page.data.size,
                        paginationMode = selectedGroup.paginationMode(),
                    )
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

    private fun loadGroupedRowPage(rowId: String) {
        val expansion = expansions.value[rowId]?.takeUnless { it.isLoading || it.reachedEnd } ?: return
        val selectedGroup = state.value.selectedGroup
        expansions.update { current -> current.put(rowId, expansion.copy(isLoading = true)) }

        viewModelScope.launch {
            notificationsRepository.getGroupNotifications(
                group = selectedGroup,
                groupId = expansion.groupId,
                page = expansion.nextPage,
            )
                .onSuccess { page ->
                    expansions.update { current ->
                        val latest = current[rowId] ?: return@update current
                        val knownIds = latest.members.mapTo(mutableSetOf()) { member -> member.id }
                        val members = (latest.members + page.data.filter { member -> knownIds.add(member.id) })
                            .toPersistentList()
                        val total = page.pagination?.total?.takeIf { it > 0 }
                        current.put(
                            rowId,
                            latest.copy(
                                members = members,
                                nextPage = latest.nextPage + 1,
                                isLoading = false,
                                reachedEnd = page.data.isEmpty() || (total != null && members.size >= total),
                            ),
                        )
                    }
                }
                .onFailure {
                    logger.error(
                        message = "Failed to load notifications of group ${expansion.groupId} in group=$selectedGroup",
                        throwable = it,
                    )
                    expansions.update { current ->
                        current[rowId]?.let { latest -> current.put(rowId, latest.copy(isLoading = false)) } ?: current
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun markTagRead(tagName: String) {
        items.update { currentItems ->
            currentItems
                .map { item -> if (item.tagName == tagName) item.copy(isRead = true) else item }
                .toPersistentList()
        }
        markMembersRead { notification -> notification.tagName == tagName }
    }

    private fun markMembersRead(predicate: (NotificationItem) -> Boolean) {
        expansions.update { current ->
            current
                .mapValues { (_, expansion) ->
                    expansion.copy(
                        members = expansion.members
                            .map { member -> if (predicate(member)) member.copy(isRead = true) else member }
                            .toPersistentList(),
                    )
                }
                .toPersistentMap()
        }
    }

    private fun NotificationGroup.paginationMode() = FeaturePaginationPolicies.notifications(this)

    private fun navigateTo(target: NotificationNavigationTarget) {
        when (target) {
            is NotificationNavigationTarget.Conversation -> {
                appNavigator.navigateTo(ConversationScreen(username = target.username))
            }

            is NotificationNavigationTarget.Entry -> {
                appNavigator.navigateTo(EntryDetailsScreen.forEntry(id = target.id))
            }

            is NotificationNavigationTarget.External -> {
                appNavigator.openLink(target.url)
            }

            is NotificationNavigationTarget.Link -> {
                appNavigator.navigateTo(LinkDetailsScreen(id = target.id))
            }

            NotificationNavigationTarget.None -> Unit

            is NotificationNavigationTarget.Profile -> {
                appNavigator.navigateTo(ProfileScreen(username = target.username))
            }

            is NotificationNavigationTarget.Tag -> {
                appNavigator.navigateTo(TagScreen(tag = target.name, content = target.content.toTagContent()))
            }
        }
    }
}

private data class GroupExpansion(
    val groupId: String,
    val members: PersistentList<NotificationItem> = persistentListOf(),
    val nextPage: Int = 1,
    val isLoading: Boolean = false,
    val reachedEnd: Boolean = false,
) {
    fun toState() = NotificationGroupExpansionState(
        items = members.toGroupMemberStates().toPersistentList(),
        isLoading = isLoading,
        canLoadMore = !isLoading && !reachedEnd,
    )
}

private fun GroupedTagContentType?.toTagContent(): TagContent = when (this) {
    GroupedTagContentType.Entry -> TagContent.Entries
    GroupedTagContentType.Link -> TagContent.Links
    GroupedTagContentType.Generic, null -> TagContent.All
}
