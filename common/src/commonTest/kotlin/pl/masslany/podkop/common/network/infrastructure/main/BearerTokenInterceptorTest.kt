package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import pl.masslany.podkop.common.network.models.request.REQUEST_HEADER_SKIP_AUTH

class BearerTokenInterceptorTest {

    @Test
    fun `skip auth header bypasses refresh and strips internal header`() = runBlocking {
        var authorizationHeader: String? = null
        var skipAuthHeader: String? = null
        val configStorage = TestConfigStorage(bearerToken = "token-123")
        val tokenRefreshHandler = FakeTokenRefreshHandler()
        val client =
            testClient(configStorage, tokenRefreshHandler) { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                skipAuthHeader = request.headers[REQUEST_HEADER_SKIP_AUTH]
                respondText("ok")
            }

        try {
            val response =
                client.get("https://wykop.pl/api/v3/links") {
                    header(REQUEST_HEADER_SKIP_AUTH, "true")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, tokenRefreshHandler.refreshIfTokenExpiringCalls)
            assertEquals(emptyList(), tokenRefreshHandler.refreshTokensCalls)
            assertNull(authorizationHeader)
            assertNull(skipAuthHeader)
        } finally {
            client.close()
        }
    }

    @Test
    fun `auth endpoints bypass token injection and refresh`() = runBlocking {
        var authorizationHeader: String? = "unexpected"
        val configStorage = TestConfigStorage(bearerToken = "token-123")
        val tokenRefreshHandler = FakeTokenRefreshHandler()
        val client =
            testClient(configStorage, tokenRefreshHandler) { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                respondText("ok")
            }

        try {
            val response = client.get("https://wykop.pl/api/v3/auth")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, tokenRefreshHandler.refreshIfTokenExpiringCalls)
            assertEquals(emptyList(), tokenRefreshHandler.refreshTokensCalls)
            assertNull(authorizationHeader)
        } finally {
            client.close()
        }
    }

    @Test
    fun `regular requests add bearer token after proactive refresh check`() = runBlocking {
        var authorizationHeader: String? = null
        val configStorage = TestConfigStorage(bearerToken = "token-123")
        val tokenRefreshHandler = FakeTokenRefreshHandler()
        val client =
            testClient(configStorage, tokenRefreshHandler) { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                respondText("ok")
            }

        try {
            val response = client.get("https://wykop.pl/api/v3/links")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, tokenRefreshHandler.refreshIfTokenExpiringCalls)
            assertEquals(emptyList(), tokenRefreshHandler.refreshTokensCalls)
            assertEquals("Bearer token-123", authorizationHeader)
        } finally {
            client.close()
        }
    }

    @Test
    fun `forbidden response retries once with refreshed bearer token`() = runBlocking {
        val seenAuthorizationHeaders = mutableListOf<String?>()
        val configStorage = TestConfigStorage(bearerToken = "old-token")
        val tokenRefreshHandler =
            FakeTokenRefreshHandler(
                refreshTokensResult = true,
                onForcedRefresh = {
                    configStorage.storeBearerToken("new-token")
                },
            )
        val client =
            testClient(configStorage, tokenRefreshHandler) { request ->
                seenAuthorizationHeaders += request.headers[HttpHeaders.Authorization]
                if (seenAuthorizationHeaders.size == 1) {
                    respondText("forbidden", status = HttpStatusCode.Forbidden)
                } else {
                    respondText("ok")
                }
            }

        try {
            val response = client.get("https://wykop.pl/api/v3/links")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, tokenRefreshHandler.refreshIfTokenExpiringCalls)
            assertEquals(listOf(true), tokenRefreshHandler.refreshTokensCalls)
            assertEquals(listOf<String?>("Bearer old-token", "Bearer new-token"), seenAuthorizationHeaders)
        } finally {
            client.close()
        }
    }

    @Test
    fun `forbidden response returns initial call when forced refresh fails`() = runBlocking {
        var engineCalls = 0
        val configStorage = TestConfigStorage(bearerToken = "old-token")
        val tokenRefreshHandler = FakeTokenRefreshHandler(refreshTokensResult = false)
        val client =
            testClient(configStorage, tokenRefreshHandler) { request ->
                engineCalls += 1
                assertEquals("Bearer old-token", request.headers[HttpHeaders.Authorization])
                respondText("forbidden", status = HttpStatusCode.Forbidden)
            }

        try {
            val response = client.get("https://wykop.pl/api/v3/links")

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(1, tokenRefreshHandler.refreshIfTokenExpiringCalls)
            assertEquals(listOf(true), tokenRefreshHandler.refreshTokensCalls)
            assertEquals(1, engineCalls)
        } finally {
            client.close()
        }
    }
}

private class FakeTokenRefreshHandler(
    private val refreshIfTokenExpiringResult: Boolean = true,
    private val refreshTokensResult: Boolean = false,
    private val onForcedRefresh: suspend () -> Unit = {},
) : TokenRefreshHandler {
    var refreshIfTokenExpiringCalls: Int = 0
        private set

    val refreshTokensCalls = mutableListOf<Boolean>()

    override suspend fun refreshIfTokenExpiring(): Boolean {
        refreshIfTokenExpiringCalls += 1
        return refreshIfTokenExpiringResult
    }

    override suspend fun refreshTokens(force: Boolean): Boolean {
        refreshTokensCalls += force
        if (force) {
            onForcedRefresh()
        }
        return refreshTokensResult
    }
}

private fun testClient(
    configStorage: TestConfigStorage,
    tokenRefreshHandler: TokenRefreshHandler,
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient {
    return HttpClient(MockEngine(handler)) {
        install(bearerTokenInterceptor) {
            this.configStorage = configStorage
            this.tokenRefreshCoordinator = tokenRefreshHandler
        }
    }
}
