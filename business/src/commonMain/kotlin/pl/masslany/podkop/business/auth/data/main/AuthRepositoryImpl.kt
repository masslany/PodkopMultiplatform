package pl.masslany.podkop.business.auth.data.main

import pl.masslany.podkop.business.auth.data.api.AuthDataSource
import pl.masslany.podkop.business.auth.domain.AuthRepository

internal class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : AuthRepository {
    override suspend fun isLoggedIn(): Boolean {
        return authDataSource.isLoggedIn()
    }

    override suspend fun getAuthToken(): Result<String> {
        return authDataSource.getAuthToken().mapCatching { it.data.token }
    }

    override suspend fun getWykopConnect(): Result<String> {
        return authDataSource.getWykopConnect().mapCatching { it.data.connectUrl }
    }

    override suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    ) {
        authDataSource.storeSessionTokens(
            token = token,
            refreshToken = refreshToken,
        )
    }

    override suspend fun shouldUpdateTokens(): Boolean {
        return authDataSource.shouldUpdateTokens()
    }

    override suspend fun updateTokens(): Result<Unit> {
        return authDataSource.updateTokens()
    }

    override suspend fun logout(): Result<Unit> {
        return authDataSource.logout()
    }
}
