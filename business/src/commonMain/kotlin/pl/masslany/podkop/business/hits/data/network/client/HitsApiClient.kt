package pl.masslany.podkop.business.hits.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.hits.data.network.api.HitsApi
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request


class HitsApiClient(
    private val apiClient: ApiClient,
) : HitsApi {
    override suspend fun getLinkHits(
        page: Any?,
        sort: String,
        year: Int?,
        month: Int?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            page?.let { put("page", it.toString()) }
            year?.let { put("year", it.toString()) }
            month?.let { put("month", it.toString().padStart(2, '0')) }
        }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/hits/links",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
