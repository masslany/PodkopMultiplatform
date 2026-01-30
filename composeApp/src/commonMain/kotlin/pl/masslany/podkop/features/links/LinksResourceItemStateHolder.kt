package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState

interface LinksResourceItemStateHolder : ResourceItemStateHolder {
    val hits: StateFlow<ImmutableList<ResourceItemState>>
    
    fun updateHits(data: List<ResourceItem>)
}