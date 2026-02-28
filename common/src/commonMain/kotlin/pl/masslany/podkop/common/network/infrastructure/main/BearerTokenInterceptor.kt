package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.network.models.request.REQUEST_HEADER_SKIP_AUTH

const val BEARER_TOKEN_INTERCEPTOR = "BEARER_TOKEN_INTERCEPTOR"

internal val bearerTokenInterceptor =
    createClientPlugin(
        BEARER_TOKEN_INTERCEPTOR,
        ::BearerTokenInterceptorPluginConfig,
    ) {
        val configStorage = pluginConfig.configStorage
        val tokenRefreshCoordinator = pluginConfig.tokenRefreshCoordinator

        suspend fun Send.Sender.proceedWithToken(request: HttpRequestBuilder): HttpClientCall {
            val shouldSkipAuth = request.headers[REQUEST_HEADER_SKIP_AUTH] == "true"
            if (shouldSkipAuth) {
                request.headers.remove(REQUEST_HEADER_SKIP_AUTH)
                return proceed(request)
            }

            if (request.url.pathSegments.any { it in SKIPPED_PATH_SEGMENTS }) {
                return proceed(request)
            }

            tokenRefreshCoordinator.refreshIfTokenExpiring()
            configStorage.getBearerToken().takeIf { it.isNotEmpty() }?.let {
                request.headers[HttpHeaders.Authorization] = "Bearer $it"
            }

            val initialCall = proceed(request)
            if (initialCall.response.status != HttpStatusCode.Forbidden) {
                return initialCall
            }

            val refreshed =
                tokenRefreshCoordinator.refreshTokens(force = true) ||
                    tokenRefreshCoordinator.refreshTokens(force = true)
            if (!refreshed) {
                return initialCall
            }

            configStorage.getBearerToken().takeIf { it.isNotEmpty() }?.let {
                request.headers[HttpHeaders.Authorization] = "Bearer $it"
            }
            return proceed(request)
        }

        on(Send) { request ->
            return@on proceedWithToken(request)
        }
    }

internal class BearerTokenInterceptorPluginConfig {
    lateinit var configStorage: ConfigStorage
    lateinit var tokenRefreshCoordinator: TokenRefreshCoordinator
}

private val SKIPPED_PATH_SEGMENTS = setOf("auth", "refresh-token")
