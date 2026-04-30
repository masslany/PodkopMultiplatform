package pl.masslany.podkop.business.favourites.data.network.main

import pl.masslany.podkop.business.favourites.data.api.FavouritesDataSource
import pl.masslany.podkop.business.favourites.data.network.api.FavouritesApi
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.common.pagination.PageRequest

class FavouritesDataSourceImpl(
    private val favouritesApi: FavouritesApi,
) : FavouritesDataSource {
    override suspend fun getFavourites(
        page: PageRequest,
        sort: String,
        resource: String?,
    ) = favouritesApi.getFavourites(
        page = page,
        sort = sort,
        resource = resource,
    )

    override suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        return favouritesApi.createFavourite(type = type, sourceId = sourceId)
    }

    override suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        return favouritesApi.deleteFavourite(type = type, sourceId = sourceId)
    }
}
