package pl.masslany.podkop.business.notifications.data.network.client

import pl.masslany.podkop.business.notifications.data.network.api.NotificationsApi
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class NotificationsApiClient(
    private val apiClient: ApiClient,
) : NotificationsApi {
    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        val request =
            Request<NotificationsStatusDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/notifications/status",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
