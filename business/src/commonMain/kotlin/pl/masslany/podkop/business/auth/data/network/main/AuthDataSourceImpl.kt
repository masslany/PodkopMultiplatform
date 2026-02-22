package pl.masslany.podkop.business.auth.data.network.main

import kotlinx.serialization.json.Json
import pl.masslany.podkop.business.auth.data.api.AuthDataSource
import pl.masslany.podkop.business.auth.data.network.api.AuthApi
import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.network.infrastructure.main.isExpiringIn

internal class AuthDataSourceImpl(
    private val authApi: AuthApi,
    private val configStorage: ConfigStorage,
    private val json: Json,
) : AuthDataSource {
    override suspend fun isLoggedIn(): Boolean {
        return configStorage.getRefreshToken().isNotBlank()
    }

    override suspend fun getAuthToken(): Result<AuthDto> {
        val key = configStorage.getApiKey()
        val secret = configStorage.getApiSecret()
        return authApi.getAuthToken(key, secret)
    }

    override suspend fun getWykopConnect(): Result<WykopConnectDto> {
        return authApi.getWykopConnect()
    }

    override suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    ) {
        configStorage.storeBearerToken(token)
        configStorage.storeRefreshToken(refreshToken)
    }

    override suspend fun shouldUpdateTokens(): Boolean {
        val token = configStorage.getBearerToken()
        if (token.isEmpty()) {
            return true
        }

        return token.isExpiringIn(JWT_EXPIRATION_LEEWAY_SECONDS, json)
    }

    override suspend fun updateTokens(): Result<Unit> {
        val refreshToken = configStorage.getRefreshToken()
        return if (refreshToken.isEmpty()) {
            getAuthToken().mapCatching {
                configStorage.storeBearerToken(it.data.token)
                Result.success(Unit)
            }
        } else {
            authApi.refreshTokens(refreshToken).mapCatching {
                configStorage.storeBearerToken(it.data.token)
                configStorage.storeRefreshToken(it.data.refreshToken)
                Result.success(Unit)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return authApi.logout().also {
            getAuthToken().mapCatching {
                configStorage.storeBearerToken(it.data.token)
                updateTokens()
            }
        }
    }

    internal companion object {
        const val JWT_EXPIRATION_LEEWAY_SECONDS = 60L
    }
}
