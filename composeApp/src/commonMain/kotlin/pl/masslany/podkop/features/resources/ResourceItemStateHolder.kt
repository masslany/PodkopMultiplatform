package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.models.ResourceItemState

interface ResourceItemStateHolder : ResourceItemActions {
    val items: StateFlow<ImmutableList<ResourceItemState>>

    fun init(scope: CoroutineScope)
    fun updateData(data: List<ResourceItem>)
}
