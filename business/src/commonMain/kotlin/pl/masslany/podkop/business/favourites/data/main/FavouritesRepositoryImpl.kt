package pl.masslany.podkop.business.favourites.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.favourites.data.api.FavouritesDataSource
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class FavouritesRepositoryImpl(
    private val favouritesDataSource: FavouritesDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : FavouritesRepository {
    override suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            favouritesDataSource.createFavourite(type = type, sourceId = sourceId)
        }
    }

    override suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            favouritesDataSource.deleteFavourite(type = type, sourceId = sourceId)
        }
    }
}
