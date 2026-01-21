package pl.masslany.podkop.business.auth.data.network.main

import pl.masslany.podkop.business.auth.data.api.AuthDataSource
import pl.masslany.podkop.business.auth.data.network.api.AuthApi
import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

internal class AuthDataSourceImpl(
    private val authApi: AuthApi,
    private val configStorage: ConfigStorage,
) : AuthDataSource {
    override suspend fun getAuthToken(): Result<AuthDto> {
        val key = configStorage.getApiKey()
        val secret = configStorage.getApiSecret()
        return authApi.getAuthToken(key, secret)
    }

    override suspend fun getWykopConnect(): Result<WykopConnectDto> {
        return authApi.getWykopConnect()
    }

    override suspend fun shouldUpdateTokens(): Boolean {
        val token = configStorage.getBearerToken()
        if (token.isEmpty()) {
            return true
        }
        //val jwt = JWT(token)

        return true
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
