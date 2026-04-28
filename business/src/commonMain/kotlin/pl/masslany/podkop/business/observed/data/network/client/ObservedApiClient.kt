package pl.masslany.podkop.business.observed.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.observed.data.network.api.ObservedApi
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class ObservedApiClient(
    private val apiClient: ApiClient,
) : ObservedApi {
    override suspend fun getObserved(
        page: PageRequest,
        type: ObservedType,
    ): Result<ObservedResponseDto> {
        val queryParameters = buildMap {
            putPagination(page)
        }.takeIf { it.isNotEmpty() }

        val request = Request<ObservedResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/observed/${type.endpointPath}",
            queryParameters = queryParameters,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
