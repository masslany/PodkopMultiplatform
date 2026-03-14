package pl.masslany.podkop.common.securestorage.infrastructure.main

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import kotlinx.coroutines.withContext
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidSecureKeyValueStorage(
    context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : SecureKeyValueStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val secretKey: SecretKey by lazy { getOrCreateSecretKey() }

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        withContext(dispatcherProvider.io) {
            if (value.isEmpty()) {
                preferences.edit {
                    remove(key)
                }
                return@withContext
            }

            preferences.edit {
                putString(key, encrypt(value))
            }
        }
    }

    override suspend fun getString(key: String): String? {
        return withContext(dispatcherProvider.io) {
            val encryptedValue = preferences.getString(key, null) ?: return@withContext null
             decrypt(encryptedValue) ?: run {
                preferences.edit {
                    remove(key)
                }
                 null
            }
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(value.encodeToByteArray())
        val payload = cipher.iv + encryptedBytes
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String? {
        return runCatching {
            val payload = Base64.decode(value, Base64.NO_WRAP)
            if (payload.size <= GCM_IV_SIZE_BYTES) {
                return null
            }

            val iv = payload.copyOfRange(0, GCM_IV_SIZE_BYTES)
            val encryptedBytes = payload.copyOfRange(GCM_IV_SIZE_BYTES, payload.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
            )

            cipher.doFinal(encryptedBytes).decodeToString()
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { existingKey ->
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    companion object {
        const val PREFERENCES_NAME = "podkop_secure_storage"

        private const val AES_KEY_SIZE_BITS = 256
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val KEY_ALIAS = "podkop_secure_storage_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
