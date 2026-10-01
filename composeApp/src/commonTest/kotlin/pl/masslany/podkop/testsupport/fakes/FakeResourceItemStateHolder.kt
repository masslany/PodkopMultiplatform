package pl.masslany.podkop.testsupport.fakes

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

/** Keeps item states in memory like the real holder, without its action handling. */
class FakeResourceItemStateHolder :
    ResourceItemStateHolder,
    ResourceItemActions by NoOpResourceItemActions {
    override val items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())

    override fun init(scope: CoroutineScope, isUpcoming: Boolean) = Unit

    override suspend fun updateData(data: List<ResourceItem>) {
        items.value = data.map { it.toResourceItemState() }.toImmutableList()
    }

    override suspend fun appendData(data: List<ResourceItem>) {
        val known = items.value.map { it.id }.toSet()
        items.value = (items.value + data.filter { it.id !in known }.map { it.toResourceItemState() }).toImmutableList()
    }

    override suspend fun notifyItemUpdated(newState: ResourceItem) = Unit
}
