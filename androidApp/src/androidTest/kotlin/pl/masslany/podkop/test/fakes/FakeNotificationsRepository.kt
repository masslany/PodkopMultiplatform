package pl.masslany.podkop.test.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationsPage
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus
import pl.masslany.podkop.common.pagination.PageRequest

class FakeNotificationsRepository : NotificationsRepository {
    private val _status = MutableStateFlow(NotificationsStatus.empty)
    override val status: StateFlow<NotificationsStatus> = _status.asStateFlow()
    override val unreadCount: StateFlow<Int> = MutableStateFlow(0).asStateFlow()

    override suspend fun refreshStatus(): Result<NotificationsStatus> =
        Result.success(NotificationsStatus.empty)

    override suspend fun getNotifications(
        group: NotificationGroup,
        page: PageRequest,
    ): Result<NotificationsPage> = Result.success(NotificationsPage.empty)

    override suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItem> =
        Result.failure(UnsupportedOperationException("Notifications are not used in integration tests yet"))

    override suspend fun markAllAsRead(group: NotificationGroup): Result<Unit> = Result.success(Unit)

    override suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAll(group: NotificationGroup): Result<Unit> = Result.success(Unit)

    override suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = Result.success(Unit)

    override fun startPolling() = Unit

    override fun stopPolling() = Unit

    override fun clearUnreadCount() {
        _status.value = NotificationsStatus.empty
    }
}
