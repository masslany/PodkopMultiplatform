package pl.masslany.podkop.business.privatemessages.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.privatemessages.data.network.api.PrivateMessagesApi
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageOpenDataDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageOpenRequestDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class PrivateMessagesApiClient(
    private val apiClient: ApiClient,
) : PrivateMessagesApi {
    override suspend fun getConversations(page: Int): Result<PrivateMessagesListDto> {
        val queryParameters = buildMap {
            putPagination(PageRequest.Number(page))
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<PrivateMessagesListDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/pm/conversations",
                queryParameters = queryParameters,
            )

        return execute(request)
    }

    override suspend fun getConversationMessages(
        username: String,
        page: Int,
    ): Result<PrivateMessageThreadDto> {
        val queryParameters = buildMap {
            putPagination(PageRequest.Number(page))
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<PrivateMessageThreadDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/pm/conversations/$username",
                queryParameters = queryParameters,
            )

        return execute(request)
    }

    override suspend fun getConversationMessagesNewer(username: String): Result<PrivateMessageThreadDto> {
        val request =
            Request<PrivateMessageThreadDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/pm/conversations/$username/newer",
            )

        return execute(request)
    }

    override suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean,
        photoKey: String?,
        embed: String?,
    ): Result<PrivateMessageItemResponseDto> {
        val request =
            Request<PrivateMessageItemResponseDto>(
                method = Request.HttpMethod.POST,
                path = "api/v3/pm/conversations/$username",
                body = PrivateMessageOpenRequestDto(
                    data = PrivateMessageOpenDataDto(
                        content = content,
                        photo = photoKey,
                        embed = embed,
                    ),
                ),
            )

        return execute(request)
    }

    override suspend fun readAll(): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.PUT,
                path = "api/v3/pm/read-all",
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
