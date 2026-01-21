package pl.masslany.podkop.common.configstorage.infrastructure.main

import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.persistence.api.KeyValueStorage

internal class BuildConfigStorage(
    private val keyValueStorage: KeyValueStorage
) : ConfigStorage {
    override suspend fun getApiKey(): String = keyValueStorage.getString(KEY).orEmpty()
    override suspend fun getApiSecret(): String = keyValueStorage.getString(SECRET).orEmpty()
    override suspend fun getBearerToken(): String = keyValueStorage.getString(TOKEN).orEmpty()
    override suspend fun getRefreshToken(): String = keyValueStorage.getString(REFRESH_TOKEN).orEmpty()

    override suspend fun storeApiKey(key: String) {
        keyValueStorage.putString(KEY, key)
    }

    override suspend fun storeApiSecret(secret: String) {
        keyValueStorage.putString(SECRET, secret)
    }

    override suspend fun storeBearerToken(token: String) {
        keyValueStorage.putString(TOKEN, token)
    }

    override suspend fun storeRefreshToken(refreshToken: String) {
        keyValueStorage.putString(REFRESH_TOKEN, refreshToken)
    }

    private companion object {
        const val KEY = "API_KEY"
        const val SECRET = "API_SECRET"
        const val TOKEN = "API_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}
