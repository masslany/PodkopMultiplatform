package pl.masslany.podkop.business.search.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.search.data.network.api.SearchApi
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class SearchApiClient(
    private val apiClient: ApiClient,
) : SearchApi {
    override suspend fun getSearchStream(
        page: Any?,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("query", query.query)
            put("sort", query.sort.value)
            query.minimumVotes?.let { put("votes", it.toString()) }
            query.dateFrom?.let { put("date_from", it) }
            query.dateTo?.let { put("date_to", it) }
            query.category?.takeIf(String::isNotBlank)?.let { put("category", it) }
            page?.let { put("page", it.toString()) }
            limit?.let { put("limit", it.toString()) }
        }.takeIf { it.isNotEmpty() }

        val queryParameterPairs = buildList {
            query.domains.forEach { add("domains[]" to it) }
            query.users.forEach { add("users[]" to it) }
            query.tags.forEach { add("tags[]" to it) }
        }.takeIf { it.isNotEmpty() }

        val request = Request<ResourceResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/search/stream",
            queryParameters = queryParameters,
            queryParameterPairs = queryParameterPairs,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
