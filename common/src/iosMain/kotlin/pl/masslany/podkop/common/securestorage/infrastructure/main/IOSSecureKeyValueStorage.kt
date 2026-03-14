@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package pl.masslany.podkop.common.securestorage.infrastructure.main

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage

class IOSSecureKeyValueStorage : SecureKeyValueStorage {
    override suspend fun putString(
        key: String,
        value: String,
    ) {
        deleteItem(key)
        if (value.isEmpty()) {
            return
        }

        withKeychainQuery(
            buildWriteQuery(
                key = key,
                value = value.encodeToByteArray().toNSData(),
            ),
        ) { query ->
            SecItemAdd(query, null)
        }
    }

    override suspend fun getString(key: String): String? {
        return memScoped {
            val result = alloc<COpaquePointerVar>()
            withKeychainQuery(buildReadQuery(key)) { query ->
                when (SecItemCopyMatching(query, result.ptr.reinterpret())) {
                    errSecSuccess -> {
                        val data = CFBridgingRelease(result.value) as? NSData ?: return@withKeychainQuery null
                        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                    }

                    errSecItemNotFound -> null
                    else -> null
                }
            }
        }
    }

    private fun deleteItem(key: String) {
        withKeychainQuery(buildBaseQuery(key)) { query ->
            SecItemDelete(query)
        }
    }

    private fun buildBaseQuery(key: String): Map<Any?, Any?> {
        return mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
        )
    }

    private fun buildReadQuery(key: String): Map<Any?, Any?> {
        return mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )
    }

    private fun buildWriteQuery(
        key: String,
        value: NSData,
    ): Map<Any?, Any?> {
        return mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            kSecValueData to value,
        )
    }

    private companion object {
        const val SERVICE_NAME = "pl.masslany.podkop.secure_storage"
    }
}

private inline fun <T> withKeychainQuery(
    query: Map<Any?, Any?>,
    block: (CFDictionaryRef) -> T,
): T {
    val cfQuery: CFDictionaryRef = CFBridgingRetain(query)?.reinterpret() ?: error("Failed to bridge keychain query")
    return try {
        block(cfQuery)
    } finally {
        CFBridgingRelease(cfQuery)
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(
        bytes = pinned.addressOf(0),
        length = size.toULong(),
    )
}
