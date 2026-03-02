package pl.masslany.podkop.business.favourites.domain.main

import pl.masslany.podkop.business.favourites.domain.models.FavouriteType

interface FavouritesRepository {
    suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>

    suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>
}
