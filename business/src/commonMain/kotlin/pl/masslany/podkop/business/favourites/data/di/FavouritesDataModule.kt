package pl.masslany.podkop.business.favourites.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.favourites.data.main.FavouritesRepositoryImpl
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository

val favouritesDataModule = module {
    single<FavouritesRepository> {
        FavouritesRepositoryImpl(
            favouritesDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
