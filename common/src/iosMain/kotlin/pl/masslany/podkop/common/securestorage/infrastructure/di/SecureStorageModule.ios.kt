package pl.masslany.podkop.common.securestorage.infrastructure.di

import org.koin.dsl.module
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage
import pl.masslany.podkop.common.securestorage.infrastructure.main.IOSSecureKeyValueStorage

actual val secureStorageModule = module {
    single<SecureKeyValueStorage> {
        IOSSecureKeyValueStorage()
    }
}
