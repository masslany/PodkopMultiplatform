package pl.masslany.podkop.common.configstorage.infrastructure.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage

class BuildConfigStorageTest {
    @Test
    fun `stores and reads config values through secure storage`() = runBlocking {
        val secureKeyValueStorage = FakeSecureKeyValueStorage()
        val sut = BuildConfigStorage(secureKeyValueStorage = secureKeyValueStorage)

        sut.storeApiKey("key")
        sut.storeApiSecret("secret")
        sut.storeBearerToken("token")
        sut.storeRefreshToken("refresh")

        assertEquals("key", sut.getApiKey())
        assertEquals("secret", sut.getApiSecret())
        assertEquals("token", sut.getBearerToken())
        assertEquals("refresh", sut.getRefreshToken())
    }

    @Test
    fun `empty values are read back as empty strings`() = runBlocking {
        val sut = BuildConfigStorage(secureKeyValueStorage = FakeSecureKeyValueStorage())

        assertEquals("", sut.getApiKey())
        assertEquals("", sut.getApiSecret())
        assertEquals("", sut.getBearerToken())
        assertEquals("", sut.getRefreshToken())
    }

    @Test
    fun `returns recently stored values even when secure storage does not persist them`() = runBlocking {
        val sut = BuildConfigStorage(secureKeyValueStorage = NonPersistingSecureKeyValueStorage())

        sut.storeApiKey("key")
        sut.storeApiSecret("secret")

        assertEquals("key", sut.getApiKey())
        assertEquals("secret", sut.getApiSecret())
    }
}

private class FakeSecureKeyValueStorage : SecureKeyValueStorage {
    private val values = mutableMapOf<String, String>()

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        if (value.isEmpty()) {
            values.remove(key)
        } else {
            values[key] = value
        }
    }

    override suspend fun getString(key: String): String? = values[key]
}

private class NonPersistingSecureKeyValueStorage : SecureKeyValueStorage {
    override suspend fun putString(
        key: String,
        value: String,
    ) = Unit

    override suspend fun getString(key: String): String? = null
}
