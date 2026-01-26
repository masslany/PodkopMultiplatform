package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

open class BaseResourceItemStateItemStateHolder(
    private val linksRepository: LinksRepository,
    private val appNavigator: AppNavigator,
) : ResourceItemStateHolder {

    protected val _items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    override val items = _items.asStateFlow()

    protected var scope: CoroutineScope? = null

    override fun init(scope: CoroutineScope) { this.scope = scope }

    override fun updateData(data: List<ResourceItem>) {
        _items.value = data.map { it.toResourceItemState() }.toImmutableList()
    }

    override fun onLinkVoteClicked(id: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                linksRepository.voteOnLink(linkId = id)
            } else {
                linksRepository.removeVoteOnLink(linkId = id)
            }

            result.onSuccess {
                linksRepository.getLink(id).onSuccess {  }
            }
        }
    }

    override fun onLinkClicked(id: Int) {
        appNavigator.navigateTo(object : NavTarget {}) //TODO: Link details
    }

    // This is open so specialized handlers can update multiple lists
//    open fun notifyItemUpdated(newState: ResourceItemState) {
//        _items.update { list -> list.map { if(it.id == newState.id) newState else it }.toImmutableList() }
//    }
}