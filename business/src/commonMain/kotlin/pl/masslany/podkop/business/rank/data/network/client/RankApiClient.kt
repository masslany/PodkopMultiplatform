package pl.masslany.podkop.business.rank.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.rank.data.network.api.RankApi
import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class RankApiClient(
    private val apiClient: ApiClient,
) : RankApi {
    override suspend fun getRank(page: Int): Result<RankResponseDto> {
        val queryParameters = buildMap {
            putPagination(PageRequest.Number(page))
        }

        val request = Request<RankResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/rank",
            queryParameters = queryParameters,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
