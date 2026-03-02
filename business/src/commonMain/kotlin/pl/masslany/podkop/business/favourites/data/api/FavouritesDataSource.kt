package pl.masslany.podkop.business.favourites.data.api

import pl.masslany.podkop.business.favourites.domain.models.FavouriteType

interface FavouritesDataSource {
    suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>

    suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>
}
