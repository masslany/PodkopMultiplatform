package pl.masslany.podkop.business.favourites.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.favourites.data.api.FavouritesDataSource
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class FavouritesRepositoryImpl(
    private val favouritesDataSource: FavouritesDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : FavouritesRepository {
    override suspend fun getFavourites(
        page: Any?,
        sortType: FavouritesSortType,
        resourceType: FavouritesResourceType,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            favouritesDataSource.getFavourites(
                page = page,
                sort = sortType.value,
                resource = resourceType.value,
            ).mapCatching {
                it.toResources()
            }
        }
    }

    override fun getFavouritesSortTypes(): List<FavouritesSortType> {
        return listOf(FavouritesSortType.Newest, FavouritesSortType.Oldest)
    }

    override fun getFavouritesResourceTypes(): List<FavouritesResourceType> {
        return listOf(
            FavouritesResourceType.All,
            FavouritesResourceType.Link,
            FavouritesResourceType.Entry,
            FavouritesResourceType.LinkComment,
            FavouritesResourceType.EntryComment,
        )
    }

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
