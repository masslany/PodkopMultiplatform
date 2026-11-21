package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.links.hits.models.toHitItemState
import pl.masslany.podkop.features.resources.models.ResourceItemState

class LinkHits {
    private val _items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    val items: StateFlow<ImmutableList<ResourceItemState>> = _items.asStateFlow()

    fun replace(data: List<ResourceItem>) {
        _items.value = data
            .filter { it.resource is Resource.Link }
            .map { it.toHitItemState() }
            .toImmutableList()
    }

    fun refresh(link: ResourceItem) {
        if (link.resource !is Resource.Link) return
        _items.update { list ->
            list.map { if (it.id == link.id) link.toHitItemState() else it }.toImmutableList()
        }
    }
}
