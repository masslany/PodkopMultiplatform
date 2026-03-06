package pl.masslany.podkop.business.notifications.data.network.main

import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.data.network.api.NotificationsApi
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

class NotificationsDataSourceImpl(
    private val notificationsApi: NotificationsApi,
) : NotificationsDataSource {
    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        return notificationsApi.getNotificationsStatus()
    }
}
