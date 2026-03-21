package pl.masslany.podkop.common.network.infrastructure.main

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenRefreshCoordinatorTest {

    @Test
    fun `refresh if token is not expiring skips network refresh`() = runBlocking {
        var networkCalls = 0
        val client =
            testHttpClient { _ ->
                networkCalls += 1
                error("Network refresh should not be called for a fresh token")
            }
        val sut =
            TokenRefreshCoordinator(
                configStorage = TestConfigStorage(bearerToken = jwtToken(expiresInSeconds = 3_600)),
                json = testJson,
                logger = RecordingLogger(),
                refreshHttpClient = client,
            )

        try {
            val actual = sut.refreshIfTokenExpiring(leewaySeconds = 60)

            assertTrue(actual)
            assertEquals(0, networkCalls)
        } finally {
            client.close()
        }
    }

    @Test
    fun `invalid jwt triggers refresh token flow and preserves previous refresh token when blank response returned`() = runBlocking {
        val oldRefreshToken = "refresh-token-1"
        val newBearerToken = jwtToken(expiresInSeconds = 3_600)
        val requestedPaths = mutableListOf<String>()
        val client =
            testHttpClient { request ->
                requestedPaths += request.url.encodedPath
                respondJson(
                    """
                    {"data":{"token":"$newBearerToken","refresh_token":" "}}
                    """.trimIndent(),
                )
            }
        val configStorage =
            TestConfigStorage(
                bearerToken = "definitely-not-a-jwt",
                refreshToken = oldRefreshToken,
            )
        val sut =
            TokenRefreshCoordinator(
                configStorage = configStorage,
                json = testJson,
                logger = RecordingLogger(),
                refreshHttpClient = client,
            )

        try {
            val actual = sut.refreshIfTokenExpiring(leewaySeconds = 60)

            assertTrue(actual)
            assertEquals(listOf("/api/v3/refresh-token"), requestedPaths)
            assertEquals(newBearerToken, configStorage.bearerToken)
            assertEquals(oldRefreshToken, configStorage.refreshToken)
        } finally {
            client.close()
        }
    }

    @Test
    fun `refresh tokens falls back to api credentials when refresh token refresh fails`() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val logger = RecordingLogger()
        val bearerToken = jwtToken(expiresInSeconds = 3_600)
        val client =
            testHttpClient { request ->
                requestedPaths += request.url.encodedPath
                when (request.url.encodedPath) {
                    "/api/v3/refresh-token" -> respondJson("{}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                    "/api/v3/auth" ->
                        respondJson(
                            """
                            {"data":{"token":"$bearerToken"}}
                            """.trimIndent(),
                        )

                    else -> error("Unexpected path: ${request.url.encodedPath}")
                }
            }
        val configStorage =
            TestConfigStorage(
                apiKey = "key",
                apiSecret = "secret",
                refreshToken = "refresh-token-1",
            )
        val sut =
            TokenRefreshCoordinator(
                configStorage = configStorage,
                json = testJson,
                logger = logger,
                refreshHttpClient = client,
            )

        try {
            val actual = sut.refreshTokens(force = true)

            assertTrue(actual)
            assertEquals(listOf("/api/v3/refresh-token", "/api/v3/auth"), requestedPaths)
            assertEquals(bearerToken, configStorage.bearerToken)
            assertEquals(1, logger.warnings.size)
        } finally {
            client.close()
        }
    }

    @Test
    fun `concurrent non forced refresh performs only one network refresh`() = runBlocking {
        var refreshCalls = 0
        val freshBearerToken = jwtToken(expiresInSeconds = 3_600)
        val client =
            testHttpClient { request ->
                assertEquals("/api/v3/refresh-token", request.url.encodedPath)
                refreshCalls += 1
                delay(100)
                respondJson(
                    """
                    {"data":{"token":"$freshBearerToken","refresh_token":"refresh-token-2"}}
                    """.trimIndent(),
                )
            }
        val configStorage =
            TestConfigStorage(
                bearerToken = jwtToken(expiresInSeconds = 10),
                refreshToken = "refresh-token-1",
            )
        val sut =
            TokenRefreshCoordinator(
                configStorage = configStorage,
                json = testJson,
                logger = RecordingLogger(),
                refreshHttpClient = client,
            )

        try {
            coroutineScope {
                val first = async { sut.refreshTokens(force = false) }
                val second = async { sut.refreshTokens(force = false) }

                assertTrue(first.await())
                assertTrue(second.await())
            }

            assertEquals(1, refreshCalls)
            assertEquals(freshBearerToken, configStorage.bearerToken)
            assertEquals("refresh-token-2", configStorage.refreshToken)
        } finally {
            client.close()
        }
    }
}
