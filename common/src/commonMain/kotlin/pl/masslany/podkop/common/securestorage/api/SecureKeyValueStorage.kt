package pl.masslany.podkop.common.securestorage.api

interface SecureKeyValueStorage {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?
}
