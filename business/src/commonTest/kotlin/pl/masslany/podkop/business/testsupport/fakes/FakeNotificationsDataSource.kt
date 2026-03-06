package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

class FakeNotificationsDataSource : NotificationsDataSource {
    var getNotificationsStatusCalls = 0
    var getNotificationsStatusResult: Result<NotificationsStatusDto> =
        unstubbedResult("getNotificationsStatus")

    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        getNotificationsStatusCalls += 1
        return getNotificationsStatusResult
    }
}
