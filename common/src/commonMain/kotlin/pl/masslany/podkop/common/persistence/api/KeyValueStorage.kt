package pl.masslany.podkop.common.persistence.api

import kotlinx.coroutines.flow.Flow

interface KeyValueStorage {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?

    suspend fun putBoolean(
        key: String,
        value: Boolean,
    )

    fun observeBoolean(key: String): Flow<Boolean?>

    suspend fun putLong(
        key: String,
        value: Long,
    )

    suspend fun getLong(key: String): Long?
}
