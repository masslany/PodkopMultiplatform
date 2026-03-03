package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.links.hits.models.toHitItemState
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdatesStore
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resources.BaseResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState

class LinksResourceItemStateHolderImpl(
    linksRepository: LinksRepository,
    entriesRepository: EntriesRepository,
    favouritesRepository: FavouritesRepository,
    appNavigator: AppNavigator,
    dispatcherProvider: DispatcherProvider,
    logger: AppLogger,
    twitterEmbedPreviewRepository: TwitterEmbedPreviewRepository,
    screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
    resourceActionUpdatesStore: ResourceActionUpdatesStore,
) : BaseResourceItemStateHolder(
    entriesRepository = entriesRepository,
    linksRepository = linksRepository,
    favouritesRepository = favouritesRepository,
    appNavigator = appNavigator,
    dispatcherProvider = dispatcherProvider,
    logger = logger,
    twitterEmbedPreviewRepository = twitterEmbedPreviewRepository,
    screenshotShareDraftStore = screenshotShareDraftStore,
    resourceActionUpdatesStore = resourceActionUpdatesStore,
),
    LinksResourceItemStateHolder {

    private val _hits = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    override val hits: StateFlow<ImmutableList<ResourceItemState>> = _hits

    override fun updateHits(data: List<ResourceItem>) {
        _hits.update {
            data
                .filter { it.resource is Resource.Link }
                .map { it.toHitItemState() }
                .toImmutableList()
        }
    }

    override suspend fun notifyItemUpdated(newState: ResourceItem) {
        super.notifyItemUpdated(newState)
        _hits.update { list ->
            list.map {
                if (it.id == newState.id && newState.resource is Resource.Link) {
                    newState.toHitItemState()
                } else {
                    it
                }
            }.toImmutableList()
        }
    }
}
