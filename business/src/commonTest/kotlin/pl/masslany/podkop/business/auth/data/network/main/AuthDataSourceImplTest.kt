package pl.masslany.podkop.business.auth.data.network.main

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import pl.masslany.podkop.business.auth.data.network.api.AuthApi
import pl.masslany.podkop.business.auth.data.network.models.RefreshDto
import pl.masslany.podkop.business.auth.data.network.models.RefreshResponseData
import pl.masslany.podkop.business.testsupport.fakes.FakeAuthApi
import pl.masslany.podkop.business.testsupport.fakes.FakeConfigStorage
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthDataSourceImplTest {

    @Test
    fun `update tokens uses refresh token response when available`() = runBlocking {
        val configStorage =
            FakeConfigStorage(
                apiKey = "k",
                apiSecret = "s",
                bearerToken = "old-token",
                refreshToken = "old-refresh",
            )
        val authApi =
            FakeAuthApi().apply {
                refreshTokensResult = Result.success(
                    Fixtures.refreshDto(
                        token = "new-token",
                        refreshToken = "new-refresh",
                    ),
                )
            }
        val sut = createSut(authApi = authApi, configStorage = configStorage)

        val actual = sut.updateTokens()

        assertTrue(actual.isSuccess)
        assertEquals("new-token", configStorage.bearerToken)
        assertEquals("new-refresh", configStorage.refreshToken)
        assertEquals(listOf("old-refresh"), authApi.refreshTokensCalls)
        assertEquals(emptyList(), authApi.getAuthTokenCalls)
    }

    @Test
    fun `update tokens keeps current refresh token when refresh response does not rotate it`() = runBlocking {
        val configStorage =
            FakeConfigStorage(
                apiKey = "k",
                apiSecret = "s",
                bearerToken = "old-token",
                refreshToken = "old-refresh",
            )
        val authApi =
            FakeAuthApi().apply {
                refreshTokensResult = Result.success(
                    RefreshDto(
                        data =
                            RefreshResponseData(
                                refreshToken = null,
                                token = "new-token",
                            ),
                    ),
                )
            }
        val sut = createSut(authApi = authApi, configStorage = configStorage)

        val actual = sut.updateTokens()

        assertTrue(actual.isSuccess)
        assertEquals("new-token", configStorage.bearerToken)
        assertEquals("old-refresh", configStorage.refreshToken)
        assertEquals(listOf("old-refresh"), authApi.refreshTokensCalls)
        assertEquals(emptyList(), authApi.getAuthTokenCalls)
    }

    @Test
    fun `update tokens falls back to auth token when refresh fails`() = runBlocking {
        val configStorage =
            FakeConfigStorage(
                apiKey = "k",
                apiSecret = "s",
                bearerToken = "old-token",
                refreshToken = "old-refresh",
            )
        val expected = IllegalStateException("refresh failed")
        val authApi =
            FakeAuthApi().apply {
                refreshTokensResult = Result.failure(expected)
                getAuthTokenResult = Result.success(Fixtures.authDto(token = "fallback-token"))
            }
        val sut = createSut(authApi = authApi, configStorage = configStorage)

        val actual = sut.updateTokens()

        assertTrue(actual.isSuccess)
        assertEquals("fallback-token", configStorage.bearerToken)
        assertEquals("old-refresh", configStorage.refreshToken)
        assertEquals(listOf("old-refresh"), authApi.refreshTokensCalls)
        assertEquals(listOf("k" to "s"), authApi.getAuthTokenCalls)
    }

    @Test
    fun `update tokens uses auth token when refresh token is absent`() = runBlocking {
        val configStorage =
            FakeConfigStorage(
                apiKey = "k",
                apiSecret = "s",
                bearerToken = "old-token",
                refreshToken = "",
            )
        val authApi =
            FakeAuthApi().apply {
                getAuthTokenResult = Result.success(Fixtures.authDto(token = "new-token"))
            }
        val sut = createSut(authApi = authApi, configStorage = configStorage)

        val actual = sut.updateTokens()

        assertTrue(actual.isSuccess)
        assertEquals("new-token", configStorage.bearerToken)
        assertEquals(emptyList(), authApi.refreshTokensCalls)
        assertEquals(listOf("k" to "s"), authApi.getAuthTokenCalls)
    }

    private fun createSut(
        authApi: AuthApi,
        configStorage: ConfigStorage,
    ): AuthDataSourceImpl {
        return AuthDataSourceImpl(
            authApi = authApi,
            configStorage = configStorage,
            json = Json { ignoreUnknownKeys = true },
        )
    }
}
