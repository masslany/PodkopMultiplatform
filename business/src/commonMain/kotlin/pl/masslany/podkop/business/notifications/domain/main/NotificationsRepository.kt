package pl.masslany.podkop.business.notifications.domain.main

import pl.masslany.podkop.common.pagination.PageRequest

import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationsPage
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus

interface NotificationsRepository {
    val status: StateFlow<NotificationsStatus>
    val unreadCount: StateFlow<Int>

    suspend fun refreshStatus(): Result<NotificationsStatus>

    suspend fun getNotifications(
        group: NotificationGroup,
        page: PageRequest = PageRequest.Initial,
    ): Result<NotificationsPage>

    suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItem>

    suspend fun markAllAsRead(group: NotificationGroup): Result<Unit>

    suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit>

    suspend fun deleteAll(group: NotificationGroup): Result<Unit>

    suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit>

    fun startPolling()

    fun stopPolling()

    fun clearUnreadCount()
}
