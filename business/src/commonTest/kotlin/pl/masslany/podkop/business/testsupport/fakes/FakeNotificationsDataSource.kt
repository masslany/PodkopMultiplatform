package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.data.network.models.NotificationItemResponseDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsListDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup

class FakeNotificationsDataSource : NotificationsDataSource {
    var getNotificationsStatusCalls = 0
    var getNotificationsStatusResult: Result<NotificationsStatusDto> =
        unstubbedResult("getNotificationsStatus")

    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        getNotificationsStatusCalls += 1
        return getNotificationsStatusResult
    }

    override suspend fun getNotifications(
        group: NotificationGroup,
        page: Any?,
    ): Result<NotificationsListDto> = unstubbedResult("getNotifications")

    override suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItemResponseDto> = unstubbedResult("getNotification")

    override suspend fun markAllAsRead(group: NotificationGroup): Result<Unit> =
        unstubbedResult("markAllAsRead")

    override suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = unstubbedResult("markAsRead")

    override suspend fun deleteAll(group: NotificationGroup): Result<Unit> =
        unstubbedResult("deleteAll")

    override suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> = unstubbedResult("delete")
}
