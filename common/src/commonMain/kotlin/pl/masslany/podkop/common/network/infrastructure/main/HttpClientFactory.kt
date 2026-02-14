package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

internal class HttpClientFactory(
    private val configStorage: ConfigStorage,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
    private val json: Json,
) {
    fun create(): HttpClient {
        return HttpClient(HttpClientEngineProvider.provide()) {
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            install(ContentNegotiation) {
                json(json)
            }

            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }

            install(bearerTokenInterceptor) {
                this.configStorage = this@HttpClientFactory.configStorage
                this.tokenRefreshCoordinator = this@HttpClientFactory.tokenRefreshCoordinator
            }

            install(Logging) {
                logger = CommonLogger()
                level = LogLevel.ALL
            }
        }
    }

    private companion object {
        const val BASE_URL = "https://wykop.pl/"
    }
}
