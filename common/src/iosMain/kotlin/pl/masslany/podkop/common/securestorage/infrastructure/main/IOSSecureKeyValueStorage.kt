@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package pl.masslany.podkop.common.securestorage.infrastructure.main

import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
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

/**
 * Stores values as generic passwords in the Keychain.
 *
 * Queries are built as CoreFoundation dictionaries with the Security framework's own CF keys.
 * A Kotlin `Map` holding those constants does not bridge to a valid query (the constants are
 * raw pointers, not Objective-C objects), which made every add and lookup fail and left the
 * session only in memory.
 */
class IOSSecureKeyValueStorage : SecureKeyValueStorage {
    override suspend fun putString(
        key: String,
        value: String,
    ) {
        deleteItem(key)
        if (value.isEmpty()) {
            return
        }
        val data = value.encodeToByteArray().toNSData()
        withQuery(key) { query ->
            CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            withRetained(data) { valueRef ->
                CFDictionaryAddValue(query, kSecValueData, valueRef)
                SecItemAdd(query, null)
            }
        }
    }

    override suspend fun getString(key: String): String? {
        return withQuery(key) { query ->
            CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
            memScoped {
                val result = alloc<COpaquePointerVar>()
                if (SecItemCopyMatching(query, result.ptr.reinterpret()) != errSecSuccess) {
                    return@memScoped null
                }
                val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            }
        }
    }

    private fun deleteItem(key: String) {
        withQuery(key) { query -> SecItemDelete(query) }
    }

    /** A mutable query for the app's service and [key]; released after [block]. */
    private inline fun <T> withQuery(key: String, block: (CFMutableDictionaryRef) -> T): T {
        val query = CFDictionaryCreateMutable(null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
            ?: error("Failed to create keychain query")
        return try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            withRetained(SERVICE_NAME) { service -> CFDictionaryAddValue(query, kSecAttrService, service) }
            withRetained(key) { account -> CFDictionaryAddValue(query, kSecAttrAccount, account) }
            block(query)
        } finally {
            CFRelease(query)
        }
    }

    private companion object {
        const val SERVICE_NAME = "pl.masslany.podkop.secure_storage"
    }
}

/** Bridges [value] to a retained CF object for the duration of [block]; the dictionary keeps its own retain. */
private inline fun <T> withRetained(value: Any, block: (CFTypeRef?) -> T): T {
    val ref: CPointer<*>? = CFBridgingRetain(value)
    return try {
        block(ref)
    } finally {
        ref?.let { CFRelease(it) }
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(
        bytes = pinned.addressOf(0),
        length = size.toULong(),
    )
}
