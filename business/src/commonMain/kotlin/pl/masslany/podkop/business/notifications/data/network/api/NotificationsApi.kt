package pl.masslany.podkop.business.notifications.data.network.api

import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

interface NotificationsApi {
    suspend fun getNotificationsStatus(): Result<NotificationsStatusDto>
}
