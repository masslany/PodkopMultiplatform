package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

const val BEARER_TOKEN_INTERCEPTOR = "BEARER_TOKEN_INTERCEPTOR"

internal val bearerTokenInterceptor =
    createClientPlugin(
        BEARER_TOKEN_INTERCEPTOR,
        ::BearerTokenInterceptorPluginConfig,
    ) {
        val configStorage = pluginConfig.configStorage
        val tokenRefreshCoordinator = pluginConfig.tokenRefreshCoordinator

        suspend fun HttpRequestBuilder.updateAuthorizationHeader() {
            headers.remove(HttpHeaders.Authorization)
            configStorage.getBearerToken()
                .takeIf { it.isNotEmpty() }
                ?.let { token ->
                    headers[HttpHeaders.Authorization] = "Bearer $token"
                }
        }

        suspend fun Send.Sender.proceedWithToken(request: HttpRequestBuilder): HttpClientCall {
            if (request.url.pathSegments.any { it in SKIPPED_PATH_SEGMENTS }) {
                return proceed(request)
            }

            tokenRefreshCoordinator.refreshIfTokenExpiring()
            request.updateAuthorizationHeader()

            var call = proceed(request)
            var forbiddenRetry = 0
            var hasForcedRefreshAttempt = false

            while (call.response.status == HttpStatusCode.Forbidden && forbiddenRetry < MAX_FORBIDDEN_RETRIES) {
                if (!hasForcedRefreshAttempt) {
                    tokenRefreshCoordinator.refreshTokens(force = true)
                    hasForcedRefreshAttempt = true
                }

                request.updateAuthorizationHeader()
                forbiddenRetry += 1
                delay(FORBIDDEN_RETRY_DELAY_MS * forbiddenRetry)
                call = proceed(request)
            }

            return call
        }

        on(Send) { request ->
            return@on proceedWithToken(request)
        }
    }

internal class BearerTokenInterceptorPluginConfig {
    lateinit var configStorage: ConfigStorage
    lateinit var tokenRefreshCoordinator: TokenRefreshCoordinator
}

private val SKIPPED_PATH_SEGMENTS = setOf("auth", "refresh-token", "logout")
private const val MAX_FORBIDDEN_RETRIES = 3
private const val FORBIDDEN_RETRY_DELAY_MS = 150L
