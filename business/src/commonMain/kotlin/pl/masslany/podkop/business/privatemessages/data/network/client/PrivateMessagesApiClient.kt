package pl.masslany.podkop.business.privatemessages.data.network.client

import pl.masslany.podkop.business.privatemessages.data.network.api.PrivateMessagesApi
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class PrivateMessagesApiClient(
    private val apiClient: ApiClient,
) : PrivateMessagesApi {
    override suspend fun getConversations(page: Any?): Result<PrivateMessagesListDto> {
        val request =
            Request<PrivateMessagesListDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/pm/conversations",
                queryParameters = page?.let { mapOf("page" to it.toString()) },
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
