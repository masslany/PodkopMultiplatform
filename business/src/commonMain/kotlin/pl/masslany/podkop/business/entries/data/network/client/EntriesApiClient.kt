package pl.masslany.podkop.business.entries.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class EntriesApiClient(
    private val apiClient: ApiClient,
) : EntriesApi {
    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        val queryParams =
            mutableMapOf(
                "sort" to sort,
                "last_update" to hotSort.toString(),
            )
        page?.let { queryParams["page"] = it.toString() }
        limit?.let { queryParams["limit"] = it.toString() }
        category?.let { queryParams["category"] = it }
        bucket?.let { queryParams["bucket"] = it }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto> {
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto> {
        val queryParams = mutableMapOf<String, String>()
        page?.let { queryParams["page"] = it.toString() }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId/comments",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries/$entryId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/entries/$entryId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
