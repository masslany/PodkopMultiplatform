package pl.masslany.podkop.business.rank.data.network.client

import pl.masslany.podkop.business.rank.data.network.api.RankApi
import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class RankApiClient(
    private val apiClient: ApiClient,
) : RankApi {
    override suspend fun getRank(page: Int): Result<RankResponseDto> {
        val request = Request<RankResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/rank",
            queryParameters = mapOf(
                "page" to page.toString(),
            ),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
