package pl.masslany.podkop.business.testsupport.fakes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.persistence.api.KeyValueStorage

class FakeDispatcherProvider(
    override val io: CoroutineDispatcher = Dispatchers.Unconfined,
    override val main: CoroutineDispatcher = Dispatchers.Unconfined,
    override val default: CoroutineDispatcher = Dispatchers.Unconfined,
) : DispatcherProvider

class FakeKeyValueStorage : KeyValueStorage {
    private val strings = mutableMapOf<String, MutableStateFlow<String?>>()
    private val booleans = mutableMapOf<String, MutableStateFlow<Boolean?>>()
    private val longs = mutableMapOf<String, Long>()

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        strings.getOrPut(key) { MutableStateFlow(null) }.value = value
    }

    override suspend fun getString(key: String): String? = strings[key]?.value

    override fun observeString(key: String): Flow<String?> {
        return strings.getOrPut(key) { MutableStateFlow(null) }
    }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        booleans.getOrPut(key) { MutableStateFlow(null) }.value = value
    }

    override fun observeBoolean(key: String): Flow<Boolean?> {
        return booleans.getOrPut(key) { MutableStateFlow(null) }
    }

    override suspend fun putLong(
        key: String,
        value: Long,
    ) {
        longs[key] = value
    }

    override suspend fun getLong(key: String): Long? = longs[key]
}
