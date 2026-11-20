package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState

class LinksResourceItemStateHolderImpl(private val linkHits: LinkHits, holder: ResourceItemStateHolder) :
    LinksResourceItemStateHolder,
    ResourceItemStateHolder by holder {

    override val hits: StateFlow<ImmutableList<ResourceItemState>> = linkHits.items

    override fun updateHits(data: List<ResourceItem>) = linkHits.replace(data)
}
