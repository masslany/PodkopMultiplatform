package pl.masslany.podkop.business.notifications.domain.main

import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus

interface NotificationsRepository {
    val unreadCount: StateFlow<Int>

    suspend fun refreshStatus(): Result<NotificationsStatus>

    fun startPolling()

    fun stopPolling()

    fun clearUnreadCount()
}
