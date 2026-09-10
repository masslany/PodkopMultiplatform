package pl.masslany.podkop.test.fakes

import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.test.IntegrationTestAuth

class FakeAuthRepository : AuthRepository {
    override suspend fun isLoggedIn(): Boolean = IntegrationTestAuth.loggedIn

    override suspend fun getAuthToken(): Result<String> =
        if (IntegrationTestAuth.loggedIn) {
            Result.success("integration-token")
        } else {
            Result.failure(IllegalStateException("Integration test user is logged out"))
        }

    override suspend fun getWykopConnect(): Result<String> =
        Result.success("https://example.test/connect")

    override suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    ) {
        IntegrationTestAuth.loggedIn = true
    }

    override suspend fun shouldUpdateTokens(): Boolean = false

    override suspend fun updateTokens(): Result<Unit> = Result.success(Unit)

    override suspend fun logout(): Result<Unit> {
        IntegrationTestAuth.loggedIn = false
        return Result.success(Unit)
    }
}
