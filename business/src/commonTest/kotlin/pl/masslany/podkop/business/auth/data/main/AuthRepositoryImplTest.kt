package pl.masslany.podkop.business.auth.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.testsupport.fakes.FakeAuthDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class AuthRepositoryImplTest {

    @Test
    fun `is logged in delegates to data source`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            isLoggedInValue = true
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.isLoggedIn()

        assertTrue(actual)
        assertEquals(1, authDataSource.isLoggedInCalls)
    }

    @Test
    fun `get auth token maps nested token value`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            getAuthTokenResult = Result.success(Fixtures.authDto(token = "token-123"))
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.getAuthToken()

        assertEquals("token-123", actual.getOrThrow())
        assertEquals(1, authDataSource.getAuthTokenCalls)
    }

    @Test
    fun `get auth token propagates failure`() = runBlocking {
        val expected = IllegalStateException("boom")
        val authDataSource = FakeAuthDataSource().apply {
            getAuthTokenResult = Result.failure(expected)
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.getAuthToken()

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `get wykop connect maps nested connect url`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            getWykopConnectResult = Result.success(Fixtures.wykopConnectDto(connectUrl = "https://connect"))
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.getWykopConnect()

        assertEquals("https://connect", actual.getOrThrow())
        assertEquals(1, authDataSource.getWykopConnectCalls)
    }

    @Test
    fun `store session tokens delegates arguments`() = runBlocking {
        val authDataSource = FakeAuthDataSource()
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        sut.storeSessionTokens(token = "t", refreshToken = "r")

        assertEquals(
            listOf(FakeAuthDataSource.StoreSessionTokensCall(token = "t", refreshToken = "r")),
            authDataSource.storeSessionTokensCalls,
        )
    }

    @Test
    fun `should update tokens delegates to data source`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            shouldUpdateTokensValue = true
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.shouldUpdateTokens()

        assertTrue(actual)
        assertEquals(1, authDataSource.shouldUpdateTokensCalls)
    }

    @Test
    fun `update tokens delegates result`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            updateTokensResult = Result.success(Unit)
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.updateTokens()

        assertTrue(actual.isSuccess)
        assertEquals(1, authDataSource.updateTokensCalls)
    }

    @Test
    fun `logout delegates result`() = runBlocking {
        val authDataSource = FakeAuthDataSource().apply {
            logoutResult = Result.success(Unit)
        }
        val sut = AuthRepositoryImpl(authDataSource = authDataSource)

        val actual = sut.logout()

        assertTrue(actual.isSuccess)
        assertEquals(1, authDataSource.logoutCalls)
    }
}
