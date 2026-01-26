package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.resources.BaseResourceItemStateItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

class LinksResourceItemStateHolderImpl(
    linksRepository: LinksRepository,
    appNavigator: AppNavigator,
) : BaseResourceItemStateItemStateHolder(
    linksRepository = linksRepository,
    appNavigator = appNavigator,
), LinksResourceItemStateHolder {

    private val _hits = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    override val hits: StateFlow<ImmutableList<ResourceItemState>> = _hits

    override fun updateHits(data: List<ResourceItem>) {
        _hits.update {
            data.map { it.toResourceItemState() }.toImmutableList()
        }
    }

    // Override to ensure both 'main' and 'hits' update when a vote happens
    override fun notifyItemUpdated(newState: ResourceItemState) {
        super.notifyItemUpdated(newState) // Updates 'items'
        _hits.update { list ->
            list.map { if (it.id == newState.id) newState else it }.toImmutableList()
        }
    }

}
