package pl.masslany.network.infrastructure

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

const val BEARER_TOKEN_INTERCEPTOR = "BEARER_TOKEN_INTERCEPTOR"

internal val bearerTokenInterceptor =
    createClientPlugin(
        BEARER_TOKEN_INTERCEPTOR,
        ::BearerTokenInterceptorPluginConfig,
    ) {
        val configStorage = pluginConfig.configStorage

        suspend fun Send.Sender.proceedWithToken(request: HttpRequestBuilder): HttpClientCall {
            if (request.url.pathSegments.contains("auth")) {
                return proceed(request)
            }

            val token = configStorage.getBearerToken()
            request.headers[HttpHeaders.Authorization] = "Bearer $token"

            return proceed(request)
        }

        on(Send) { request ->
            return@on proceedWithToken(request)
        }
    }

internal class BearerTokenInterceptorPluginConfig {
    lateinit var configStorage: ConfigStorage
}
