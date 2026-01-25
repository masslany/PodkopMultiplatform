package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.models.ResourceItemActions
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

class ResourceItemStateHolder : ResourceItemActions {

    val state: StateFlow<ImmutableList<ResourceItemState>>
        field = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())


    fun update(data: List<ResourceItem>) {
        state.update {
            data.map{ it.toResourceItemState() }.toImmutableList()
        }
    }

}