package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.business.auth.domain.AuthRepository

class FakeAuthRepository(private val loggedIn: Boolean = false) : AuthRepository {
    override suspend fun isLoggedIn(): Boolean = loggedIn
    override suspend fun getAuthToken(): Result<String> = notUsed()
    override suspend fun getWykopConnect(): Result<String> = notUsed()
    override suspend fun storeSessionTokens(token: String, refreshToken: String) = error("not used")
    override suspend fun shouldUpdateTokens(): Boolean = error("not used")
    override suspend fun updateTokens(): Result<Unit> = notUsed()
    override suspend fun logout(): Result<Unit> = notUsed()
}
