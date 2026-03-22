package pl.masslany.podkop.business.tags.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.tags.data.network.api.TagsApi
import pl.masslany.podkop.business.tags.data.network.models.TagDetailsResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class TagsApiClient(
    private val apiClient: ApiClient,
) : TagsApi {
    override suspend fun getTagDetails(tagName: String): Result<TagDetailsResponseDto> {
        val request =
            Request<TagDetailsResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/tags/$tagName",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun observeTag(tagName: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/observed/tags/$tagName",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun unobserveTag(tagName: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/observed/tags/$tagName",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun enableTagNotifications(tagName: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.PUT,
                path = "api/v3/observed/tags/$tagName/notifications",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun disableTagNotifications(tagName: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/observed/tags/$tagName/notifications",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            put("type", type)
            page?.let { put("page", it.toString()) }
            limit?.let { put("limit", it.toString()) }
        }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/tags/$tagName/stream",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getTagsAutoComplete(query: String): Result<TagsAutoCompleteResponseDto> {
        val queryParameters = buildMap {
            put("query", query)
        }
        val request =
            Request<TagsAutoCompleteResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/tags/autocomplete",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
