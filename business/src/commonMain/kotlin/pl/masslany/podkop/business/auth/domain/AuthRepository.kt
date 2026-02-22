package pl.masslany.podkop.business.auth.domain

interface AuthRepository {
    suspend fun isLoggedIn(): Boolean

    suspend fun getAuthToken(): Result<String>

    suspend fun getWykopConnect(): Result<String>

    suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    )

    suspend fun shouldUpdateTokens(): Boolean

    suspend fun updateTokens(): Result<Unit>

    suspend fun logout(): Result<Unit>
}
