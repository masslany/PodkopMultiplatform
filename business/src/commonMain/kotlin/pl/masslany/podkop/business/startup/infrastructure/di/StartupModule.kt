package pl.masslany.podkop.business.startup.infrastructure.di

import org.koin.dsl.module
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.infrastructure.main.StartupManagerImpl

val startupModule = module {
    single<StartupManager> {
        StartupManagerImpl(
            configStorage = get(),
            authRepository = get()
        )
    }
}