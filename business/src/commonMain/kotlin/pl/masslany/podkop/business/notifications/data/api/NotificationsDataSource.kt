package pl.masslany.podkop.business.notifications.data.api

import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

interface NotificationsDataSource {
    suspend fun getNotificationsStatus(): Result<NotificationsStatusDto>
}
