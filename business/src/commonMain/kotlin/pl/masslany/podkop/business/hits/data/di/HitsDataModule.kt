package pl.masslany.podkop.business.hits.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.hits.data.main.HitsRepositoryImpl
import pl.masslany.podkop.business.hits.domain.main.HitsRepository

val hitsDataModule = module {
    single<HitsRepository> {
        HitsRepositoryImpl(
            hitsDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
