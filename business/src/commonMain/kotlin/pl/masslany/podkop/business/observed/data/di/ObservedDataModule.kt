package pl.masslany.podkop.business.observed.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.observed.data.main.ObservedRepositoryImpl
import pl.masslany.podkop.business.observed.domain.main.ObservedRepository

val observedDataModule = module {
    factory<ObservedRepository> {
        ObservedRepositoryImpl(
            observedDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
