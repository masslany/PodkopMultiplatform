package pl.masslany.podkop.features.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemActions

class LinksViewModel(
    val isUpcoming: Boolean,
    val linksRepository: LinksRepository,
    val resourceItemStateHolder: ResourceItemStateHolder,
) : ViewModel(), LinksActions, ResourceItemActions by resourceItemStateHolder {

    private val _state = MutableStateFlow(LinksScreenState.initial)
    val state = combine(
        _state,
        resourceItemStateHolder.state,
    ) { state, resourceItems ->
        state.copy(
            resourceItems = resourceItems,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), LinksScreenState.initial)

    init {
        _state.update {
            it.copy(
                isUpcoming = isUpcoming,
            )
        }

        viewModelScope.launch {
            linksRepository.getLinks(
                page = 1,
                limit = null,
                linksSortType = LinksSortType.Newest,
                linksType = if (isUpcoming) LinksType.UPCOMING else LinksType.HOMEPAGE,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.update(it.data)
                }
                .onFailure {
                    println("MEOW failed to load with $it")
                }
        }
    }
    
    override fun onLinkClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onLinkVoteClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onHitClicked(id: String) {
        TODO("Not yet implemented")
    }

    override fun onHitVoteClicked(id: String) {
        TODO("Not yet implemented")
    }

}
