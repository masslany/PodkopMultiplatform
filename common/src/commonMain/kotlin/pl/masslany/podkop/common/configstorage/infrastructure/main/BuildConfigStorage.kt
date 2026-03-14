package pl.masslany.podkop.common.configstorage.infrastructure.main

import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage

internal class BuildConfigStorage(
    private val secureKeyValueStorage: SecureKeyValueStorage,
) : ConfigStorage {
    private val inMemoryValues = mutableMapOf<String, String>()

    override suspend fun getApiKey(): String = getValue(KEY)

    override suspend fun getApiSecret(): String = getValue(SECRET)

    override suspend fun getBearerToken(): String = getValue(TOKEN)

    override suspend fun getRefreshToken(): String = getValue(REFRESH_TOKEN)

    override suspend fun storeApiKey(key: String) {
        storeValue(KEY, key)
    }

    override suspend fun storeApiSecret(secret: String) {
        storeValue(SECRET, secret)
    }

    override suspend fun storeBearerToken(token: String) {
        storeValue(TOKEN, token)
    }

    override suspend fun storeRefreshToken(refreshToken: String) {
        storeValue(REFRESH_TOKEN, refreshToken)
    }

    private suspend fun getValue(key: String): String {
        inMemoryValues[key]?.let { return it }

        return secureKeyValueStorage.getString(key)
            .orEmpty()
            .also { restoredValue ->
                if (restoredValue.isNotEmpty()) {
                    inMemoryValues[key] = restoredValue
                }
            }
    }

    private suspend fun storeValue(
        key: String,
        value: String,
    ) {
        if (value.isEmpty()) {
            inMemoryValues.remove(key)
        } else {
            inMemoryValues[key] = value
        }
        secureKeyValueStorage.putString(key, value)
    }

    private companion object {
        const val KEY = "API_KEY"
        const val SECRET = "API_SECRET"
        const val TOKEN = "API_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}
