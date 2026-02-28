package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.common.configstorage.api.ConfigStorage

class FakeConfigStorage(
    private var apiKey: String = "",
    private var apiSecret: String = "",
    var bearerToken: String = "",
    var refreshToken: String = "",
) : ConfigStorage {
    override suspend fun getApiKey(): String = apiKey

    override suspend fun getApiSecret(): String = apiSecret

    override suspend fun getBearerToken(): String = bearerToken

    override suspend fun getRefreshToken(): String = refreshToken

    override suspend fun storeApiKey(key: String) {
        apiKey = key
    }

    override suspend fun storeApiSecret(secret: String) {
        apiSecret = secret
    }

    override suspend fun storeBearerToken(token: String) {
        bearerToken = token
    }

    override suspend fun storeRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }
}
