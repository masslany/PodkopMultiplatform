package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import pl.masslany.network.infrastructure.bearerTokenInterceptor
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

class HttpClientFactory(
    private val configStorage: ConfigStorage,
) {
    fun create(): HttpClient {
        return HttpClient(HttpClientEngineProvider.provide()) {
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }

            install(bearerTokenInterceptor) {
                this.configStorage = this@HttpClientFactory.configStorage
            }

            install(HttpRequestRetry) {
                maxRetries = MAX_RETRY_COUNT
                retryIf { _, httpResponse -> httpResponse.status == HttpStatusCode.Forbidden }
                delayMillis { retry -> retry * DELAY_MILLIS }
                modifyRequest {
                    runBlocking {
                        // TODO: Find a way to refresh token on 403
                        /*
                         * This still feels like a hacky approach to a scenario where token expires
                         * during a session. We want to refresh the token seamlessly during it.
                         * Maybe future me has a better knowledge on how to handle it.
                         * */
//                        withContext(Dispatchers.Default) {
//                            refreshTokensEventReducer.reduce(RefreshAction.Refresh)
//                        }
                        val token = configStorage.getBearerToken()
                        if (token.isNotEmpty()) {
                            it.headers[HttpHeaders.Authorization] = "Bearer $token"
                        }
                    }
                }
            }

            install(Logging) {
                logger = CommonLogger()
                level = LogLevel.ALL
            }
        }
    }

    private companion object {
        const val BASE_URL = "https://wykop.pl/"
        const val MAX_RETRY_COUNT = 3
        const val DELAY_MILLIS = 500L
    }
}
