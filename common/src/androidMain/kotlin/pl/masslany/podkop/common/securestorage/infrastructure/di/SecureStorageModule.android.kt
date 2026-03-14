package pl.masslany.podkop.common.securestorage.infrastructure.di

import android.content.Context
import org.koin.dsl.module
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage
import pl.masslany.podkop.common.securestorage.infrastructure.main.AndroidSecureKeyValueStorage

actual val secureStorageModule = module {
    single<SecureKeyValueStorage> {
        AndroidSecureKeyValueStorage(
            context = get<Context>(),
            dispatcherProvider = get(),
        )
    }
}
