package pl.masslany.podkop.business.favourites.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.favourites.data.api.FavouritesDataSource
import pl.masslany.podkop.business.favourites.data.network.api.FavouritesApi
import pl.masslany.podkop.business.favourites.data.network.client.FavouritesApiClient
import pl.masslany.podkop.business.favourites.data.network.main.FavouritesDataSourceImpl

val favouritesNetworkModule = module {
    single<FavouritesApi> { FavouritesApiClient(apiClient = get()) }
    single<FavouritesDataSource> { FavouritesDataSourceImpl(favouritesApi = get()) }
}
