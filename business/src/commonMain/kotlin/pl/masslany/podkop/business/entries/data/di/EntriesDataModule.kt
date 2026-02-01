package pl.masslany.podkop.business.entries.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.entries.data.main.EntriesRepositoryImpl
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository

val entriesDataModule = module {
    factory<EntriesRepository> {
        EntriesRepositoryImpl(
            entriesDataSource = get(),
            dispatcherProvider = get(),
            keyValueStorage = get(),
        )
    }
}