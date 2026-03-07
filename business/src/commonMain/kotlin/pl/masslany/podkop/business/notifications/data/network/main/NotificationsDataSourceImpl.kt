package pl.masslany.podkop.business.notifications.data.network.main

import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.data.network.api.NotificationsApi
import pl.masslany.podkop.business.notifications.data.network.models.NotificationItemResponseDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsListDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup

class NotificationsDataSourceImpl(
    private val notificationsApi: NotificationsApi,
) : NotificationsDataSource {
    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        return notificationsApi.getNotificationsStatus()
    }

    override suspend fun getNotifications(
        group: NotificationGroup,
        page: Any?,
    ): Result<NotificationsListDto> {
        return notificationsApi.getNotifications(group = group, page = page)
    }

    override suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItemResponseDto> {
        return notificationsApi.getNotification(group = group, id = id)
    }

    override suspend fun markAllAsRead(group: NotificationGroup): Result<Unit> {
        return notificationsApi.markAllAsRead(group)
    }

    override suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> {
        return notificationsApi.markAsRead(group = group, id = id)
    }

    override suspend fun deleteAll(group: NotificationGroup): Result<Unit> {
        return notificationsApi.deleteAll(group)
    }

    override suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> {
        return notificationsApi.delete(group = group, id = id)
    }
}
