package pl.masslany.podkop.business.notifications.data.main

import pl.masslany.podkop.common.pagination.PageRequest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationsPage
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus
import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.main.toPrivateMessagesPage
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.pagination.requireNumber
import kotlin.time.Duration.Companion.minutes

class NotificationsRepositoryImpl(
    private val notificationsDataSource: NotificationsDataSource,
    private val privateMessagesDataSource: PrivateMessagesDataSource,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val appScope: CoroutineScope,
) : NotificationsRepository {
    private val _status = MutableStateFlow(NotificationsStatus.empty)
    override val status: StateFlow<NotificationsStatus> = _status.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val refreshMutex = Mutex()
    private var pollingJob: Job? = null

    override suspend fun refreshStatus(): Result<NotificationsStatus> {
        return refreshMutex.withLock {
            withContext(dispatcherProvider.io) {
                if (!authRepository.isLoggedIn()) {
                    updateStatus(NotificationsStatus.empty)
                    return@withContext Result.success(NotificationsStatus.empty)
                }

                notificationsDataSource.getNotificationsStatus()
                    .mapCatching { it.toNotificationsStatus() }
                    .onSuccess { status ->
                        updateStatus(status)
                    }
            }
        }
    }

    override suspend fun getNotifications(
        group: NotificationGroup,
        page: PageRequest,
    ): Result<NotificationsPage> = withContext(dispatcherProvider.io) {
        when (group) {
            NotificationGroup.PrivateMessages -> {
                privateMessagesDataSource.getConversations(page = page.requireNumber())
                    .mapCatching { it.toPrivateMessagesPage().toNotificationsPage() }
            }

            else -> {
                notificationsDataSource.getNotifications(group = group, page = page)
                    .mapCatching { it.toNotificationsPage(group = group) }
            }
        }
    }

    override suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItem> = withContext(dispatcherProvider.io) {
        notificationsDataSource.getNotification(group = group, id = id)
            .mapCatching { it.toNotificationItem(group = group) }
    }

    override suspend fun markAllAsRead(group: NotificationGroup): Result<Unit> =
        withContext(dispatcherProvider.io) {
            notificationsDataSource.markAllAsRead(group)
                .onSuccess {
                    updateStatus(
                        _status.value.withUnreadCount(
                            group = group,
                            unreadCount = 0,
                        ),
                    )
                }
        }

    override suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = withContext(dispatcherProvider.io) {
        notificationsDataSource.markAsRead(group = group, id = id)
            .onSuccess {
                updateStatus(_status.value.decrementUnreadCount(group))
            }
    }

    override suspend fun deleteAll(group: NotificationGroup): Result<Unit> =
        withContext(dispatcherProvider.io) {
            val result = notificationsDataSource.deleteAll(group)
            if (result.isSuccess) {
                refreshStatus()
            }
            result
        }

    override suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = withContext(dispatcherProvider.io) {
        val result = notificationsDataSource.delete(group = group, id = id)
        if (result.isSuccess) {
            refreshStatus()
        }
        result
    }

    override fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = appScope.launch {
            refreshStatus()

            while (isActive) {
                delay(POLLING_INTERVAL)
                refreshStatus()
            }
        }
    }

    override fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun clearUnreadCount() {
        updateStatus(NotificationsStatus.empty)
    }

    private fun updateStatus(status: NotificationsStatus) {
        _status.value = status
        _unreadCount.value = status.totalUnreadCount
    }

    private companion object {
        val POLLING_INTERVAL = 2.minutes
    }
}
