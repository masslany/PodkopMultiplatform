package pl.masslany.podkop.business.favourites.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType

interface FavouritesRepository {
    suspend fun getFavourites(
        page: Any?,
        sortType: FavouritesSortType,
        resourceType: FavouritesResourceType,
    ): Result<Resources>

    fun getFavouritesSortTypes(): List<FavouritesSortType>

    fun getFavouritesResourceTypes(): List<FavouritesResourceType>

    suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>

    suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>
}
