package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

interface LinksResourceItemStateHolder : ResourceItemStateHolder {
    val hits: StateFlow<ImmutableList<HitItemState>>
    
    fun updateHits(data: List<ResourceItem>)
}