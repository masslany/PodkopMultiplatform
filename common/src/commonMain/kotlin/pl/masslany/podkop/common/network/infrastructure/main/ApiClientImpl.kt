package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.util.reflect.TypeInfo
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.network.models.response.ApiResponse
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.infrastructure.main.mapper.toHttpRequestBuilder
import pl.masslany.podkop.common.network.models.response.ResponseTypeInfo

internal class ApiClientImpl(
    private val httpClient: HttpClient,
) : ApiClient() {
    override suspend fun <T> executeRequest(
        request: Request<T>,
        responseType: ResponseTypeInfo,
    ): Result<ApiResponse<T>> {
        return try {
            val requestBuilder = request.toHttpRequestBuilder()
            val httpResponse = httpClient.request(requestBuilder)

            val responseBody =
                httpResponse.body(
                    TypeInfo(
                        responseType.type,
                        responseType.kotlinType,
                    ),
                ) as T

            Result.success(ApiResponse(content = responseBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
