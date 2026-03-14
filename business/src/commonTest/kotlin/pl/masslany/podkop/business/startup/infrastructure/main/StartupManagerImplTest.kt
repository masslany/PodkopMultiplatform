package pl.masslany.podkop.business.startup.infrastructure.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.business.testsupport.fakes.FakeConfigStorage
import pl.masslany.podkop.common.logging.api.AppLogger

class StartupManagerImplTest {

    @Test
    fun `init stores api credentials and becomes ready when tokens are already fresh`() = runBlocking {
        val configStorage = FakeConfigStorage()
        val authRepository = FakeAuthRepository(
            shouldUpdateTokensValue = false,
        )
        val logger = RecordingLogger()
        val sut = StartupManagerImpl(
            configStorage = configStorage,
            authRepository = authRepository,
            logger = logger,
        )

        sut.init(key = "key-123", secret = "secret-456")

        assertEquals("key-123", configStorage.getApiKey())
        assertEquals("secret-456", configStorage.getApiSecret())
        assertEquals(AppState.Ready, sut.state.value)
        assertEquals(1, authRepository.shouldUpdateTokensCalls)
        assertEquals(0, authRepository.updateTokensCalls)
        assertEquals(emptyList(), logger.errors)
    }

    @Test
    fun `init becomes ready when token refresh succeeds`() = runBlocking {
        val authRepository = FakeAuthRepository(
            shouldUpdateTokensValue = true,
            updateTokensResult = Result.success(Unit),
        )
        val sut = StartupManagerImpl(
            configStorage = FakeConfigStorage(),
            authRepository = authRepository,
            logger = RecordingLogger(),
        )

        sut.init(key = "k", secret = "s")

        assertEquals(AppState.Ready, sut.state.value)
        assertEquals(1, authRepository.updateTokensCalls)
    }

    @Test
    fun `init emits error and logs when token refresh fails`() = runBlocking {
        val failure = IllegalStateException("refresh failed")
        val logger = RecordingLogger()
        val authRepository = FakeAuthRepository(
            shouldUpdateTokensValue = true,
            updateTokensResult = Result.failure(failure),
        )
        val sut = StartupManagerImpl(
            configStorage = FakeConfigStorage(),
            authRepository = authRepository,
            logger = logger,
        )

        sut.init(key = "k", secret = "s")

        assertEquals(AppState.Error, sut.state.value)
        assertEquals(
            listOf(RecordingLogger.Error("Failed to get auth token", failure)),
            logger.errors,
        )
    }

    @Test
    fun `retry uses stored credentials and becomes ready`() = runBlocking {
        val configStorage = FakeConfigStorage(
            apiKey = "stored-key",
            apiSecret = "stored-secret",
        )
        val authRepository = FakeAuthRepository(
            shouldUpdateTokensValue = false,
        )
        val sut = StartupManagerImpl(
            configStorage = configStorage,
            authRepository = authRepository,
            logger = RecordingLogger(),
        )

        sut.retry()

        assertEquals(AppState.Ready, sut.state.value)
        assertEquals(1, authRepository.shouldUpdateTokensCalls)
        assertEquals(0, authRepository.updateTokensCalls)
    }
}

private class FakeAuthRepository(
    private val shouldUpdateTokensValue: Boolean,
    private val updateTokensResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    var shouldUpdateTokensCalls = 0
    var updateTokensCalls = 0

    override suspend fun isLoggedIn(): Boolean = false

    override suspend fun getAuthToken(): Result<String> = Result.failure(UnsupportedOperationException())

    override suspend fun getWykopConnect(): Result<String> = Result.failure(UnsupportedOperationException())

    override suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    ) = Unit

    override suspend fun shouldUpdateTokens(): Boolean {
        shouldUpdateTokensCalls += 1
        return shouldUpdateTokensValue
    }

    override suspend fun updateTokens(): Result<Unit> {
        updateTokensCalls += 1
        return updateTokensResult
    }

    override suspend fun logout(): Result<Unit> = Result.failure(UnsupportedOperationException())
}

private class RecordingLogger : AppLogger {
    data class Error(
        val message: String,
        val throwable: Throwable?,
    )

    val errors = mutableListOf<Error>()

    override fun debug(message: String) = Unit

    override fun info(message: String) = Unit

    override fun warn(
        message: String,
        throwable: Throwable?,
    ) = Unit

    override fun error(
        message: String,
        throwable: Throwable?,
    ) {
        errors += Error(message = message, throwable = throwable)
    }
}
