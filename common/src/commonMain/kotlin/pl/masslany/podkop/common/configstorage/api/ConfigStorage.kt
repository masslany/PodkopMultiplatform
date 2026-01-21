package pl.masslany.podkop.common.configstorage.api

interface ConfigStorage {
    suspend fun getApiKey(): String

    suspend fun getApiSecret(): String

    suspend fun getBearerToken(): String

    suspend fun getRefreshToken(): String

    suspend fun storeApiKey(key: String)

    suspend fun storeApiSecret(secret: String)

    suspend fun storeBearerToken(token: String)

    suspend fun storeRefreshToken(refreshToken: String)
}
