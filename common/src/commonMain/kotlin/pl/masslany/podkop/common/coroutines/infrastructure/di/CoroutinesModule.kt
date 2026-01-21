package pl.masslany.podkop.common.coroutines.infrastructure.di

import org.koin.dsl.module
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.coroutines.infrastructure.main.DispatcherProviderImpl

val coroutinesModule = module {

    factory<DispatcherProvider> {
        DispatcherProviderImpl()
    }

}
