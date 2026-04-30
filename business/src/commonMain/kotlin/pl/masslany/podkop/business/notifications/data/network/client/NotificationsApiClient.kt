package pl.masslany.podkop.business.notifications.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.notifications.data.network.api.NotificationsApi
import pl.masslany.podkop.business.notifications.data.network.models.NotificationItemResponseDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsListDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class NotificationsApiClient(
    private val apiClient: ApiClient,
) : NotificationsApi {
    override suspend fun getNotificationsStatus(): Result<NotificationsStatusDto> {
        val request =
            Request<NotificationsStatusDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/notifications/status",
            )

        return execute(request)
    }

    override suspend fun getNotifications(
        group: NotificationGroup,
        page: PageRequest,
    ): Result<NotificationsListDto> {
        val queryParameters = buildMap {
            putPagination(page)
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<NotificationsListDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/notifications/${group.pathSegment}",
                queryParameters = queryParameters,
            )

        return execute(request)
    }

    override suspend fun getNotification(
        group: NotificationGroup,
        id: String,
    ): Result<NotificationItemResponseDto> {
        val request =
            Request<NotificationItemResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/notifications/${group.pathSegment}/$id",
            )

        return execute(request)
    }

    override suspend fun markAllAsRead(group: NotificationGroup): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.PUT,
                path = "api/v3/notifications/${group.pathSegment}/all",
            )

        return execute(request)
    }

    override suspend fun markAsRead(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.PUT,
                path = "api/v3/notifications/${group.pathSegment}/$id",
            )

        return execute(request)
    }

    override suspend fun deleteAll(group: NotificationGroup): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/notifications/${group.pathSegment}/all",
            )

        return execute(request)
    }

    override suspend fun delete(
        group: NotificationGroup,
        id: String,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/notifications/${group.pathSegment}/$id",
            )

        return execute(request)
    }

    private suspend inline fun <reified T> execute(request: Request<T>): Result<T> {
        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
