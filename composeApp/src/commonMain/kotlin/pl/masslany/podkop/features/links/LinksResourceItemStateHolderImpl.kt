package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.links.hits.models.toHitItemState
import pl.masslany.podkop.features.resources.BaseResourceItemStateItemStateHolder

class LinksResourceItemStateHolderImpl(
    linksRepository: LinksRepository,
    appNavigator: AppNavigator,
) : BaseResourceItemStateItemStateHolder(
    linksRepository = linksRepository,
    appNavigator = appNavigator,
), LinksResourceItemStateHolder {

    private val _hits = MutableStateFlow<ImmutableList<HitItemState>>(persistentListOf())
    override val hits: StateFlow<ImmutableList<HitItemState>> = _hits

    override fun updateHits(data: List<ResourceItem>) {
        _hits.update {
            data
                .filter { it.resource is Resource.Link }
                .map { it.toHitItemState() }
                .toImmutableList()
        }
    }

    // Override to ensure both 'main' and 'hits' update when a vote happens
//    override fun notifyItemUpdated(newState: ResourceItemState) {
//        super.notifyItemUpdated(newState) // Updates 'items'
//        _hits.update { list ->
//            list.map { if (it.id == newState.id) newState else it }.toImmutableList()
//        }
//    }

}
