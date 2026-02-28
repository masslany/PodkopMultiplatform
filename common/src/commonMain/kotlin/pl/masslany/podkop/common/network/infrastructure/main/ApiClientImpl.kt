package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
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
            if (!httpResponse.status.isSuccess()) {
                val responseBody = runCatching { httpResponse.bodyAsText() }.getOrNull()
                throw ApiHttpException(
                    status = httpResponse.status,
                    responseBody = responseBody,
                )
            }

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

internal class ApiHttpException(
    val status: HttpStatusCode,
    val responseBody: String?,
) : IllegalStateException(
    buildString {
        append("HTTP ")
        append(status.value)
        append(' ')
        append(status.description)
        responseBody
            ?.takeIf(String::isNotBlank)
            ?.let {
                append(": ")
                append(it.take(MAX_ERROR_BODY_LENGTH))
            }
    },
) {
    private companion object {
        const val MAX_ERROR_BODY_LENGTH = 500
    }
}
