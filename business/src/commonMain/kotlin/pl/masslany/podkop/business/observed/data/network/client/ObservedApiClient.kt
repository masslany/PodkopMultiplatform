package pl.masslany.podkop.business.observed.data.network.client

import pl.masslany.podkop.business.observed.data.network.api.ObservedApi
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class ObservedApiClient(
    private val apiClient: ApiClient,
) : ObservedApi {
    override suspend fun getObserved(
        page: Any?,
        type: ObservedType,
    ): Result<ObservedResponseDto> {
        val queryParameters = buildMap {
            page?.let { put("page", it.toString()) }
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
