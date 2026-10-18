package pl.masslany.podkop.features.resources

import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.features.resources.models.withFavourite

internal class ResourceFavourites(
    private val favouritesRepository: FavouritesRepository,
    private val store: ResourceItemsStore,
    private val logger: AppLogger,
) {
    suspend fun toggle(itemId: Int, type: FavouriteType, favourited: Boolean) {
        val result = if (favourited) {
            favouritesRepository.deleteFavourite(type = type, sourceId = itemId)
        } else {
            favouritesRepository.createFavourite(type = type, sourceId = itemId)
        }

        result
            .onSuccess { store.update(itemId) { it.withFavourite(!favourited) } }
            .onFailure { logger.error("Failed to toggle favourite for itemId=$itemId type=$type", it) }
    }
}
