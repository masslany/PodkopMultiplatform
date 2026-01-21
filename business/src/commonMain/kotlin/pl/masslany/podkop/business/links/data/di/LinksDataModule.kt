package pl.masslany.podkop.business.links.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.links.data.main.LinksRepositoryImpl
import pl.masslany.podkop.business.links.domain.main.LinksRepository

val linksDataModule = module {
    single<LinksRepository> {
        LinksRepositoryImpl(
            linksDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
