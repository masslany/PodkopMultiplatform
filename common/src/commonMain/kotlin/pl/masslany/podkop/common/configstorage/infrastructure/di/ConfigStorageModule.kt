package pl.masslany.podkop.common.configstorage.infrastructure.di

import org.koin.dsl.module
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.configstorage.infrastructure.main.BuildConfigStorage
import pl.masslany.podkop.common.securestorage.api.SecureKeyValueStorage

val configStorageModule = module {
    single<ConfigStorage> {
        BuildConfigStorage(
            secureKeyValueStorage = get<SecureKeyValueStorage>(),
        )
    }
}
