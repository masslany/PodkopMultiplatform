package pl.masslany.podkop

import org.koin.dsl.module
import pl.masslany.podkop.common.logging.infrastructure.di.loggingModule
import pl.masslany.podkop.common.configstorage.infrastructure.di.configStorageModule
import pl.masslany.podkop.common.coroutines.infrastructure.di.coroutinesModule
import pl.masslany.podkop.common.network.infrastructure.di.networkModule
import pl.masslany.podkop.common.persistence.infrastructure.di.persistenceModule

val commonModule = module {
    includes(
        loggingModule,
        coroutinesModule,
        networkModule,
        persistenceModule,
        configStorageModule,
    )
}
