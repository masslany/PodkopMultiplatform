package pl.masslany.podkop.business.favourites.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.common.pagination.PageRequest

interface FavouritesDataSource {
    suspend fun getFavourites(
        page: PageRequest,
        sort: String,
        resource: String?,
    ): Result<ResourceResponseDto>

    suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>

    suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit>
}
