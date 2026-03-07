package pl.masslany.podkop.business.notifications.data.network.api

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.data.network.models.NotificationItemResponseDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsListDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

interface NotificationsApi {
    suspend fun getNotificationsStatus(): Result<NotificationsStatusDto>

    suspend fun getNotifications(
        group: NotificationGroup,
        page: Any? = null,
    ): Result<NotificationsListDto>

    suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItemResponseDto>

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
}
