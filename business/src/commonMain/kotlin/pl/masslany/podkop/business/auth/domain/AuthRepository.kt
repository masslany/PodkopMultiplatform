package pl.masslany.podkop.business.auth.domain

interface AuthRepository {
    suspend fun getAuthToken(): Result<String>

    suspend fun getWykopConnect(): Result<String>

    suspend fun shouldUpdateTokens(): Boolean

    suspend fun updateTokens(): Result<Unit>

    suspend fun logout(): Result<Unit>
}
