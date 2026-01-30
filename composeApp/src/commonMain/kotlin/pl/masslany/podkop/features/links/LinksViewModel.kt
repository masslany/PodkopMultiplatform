package pl.masslany.podkop.features.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.toDropdownMenuItemType

class LinksViewModel(
    val isUpcoming: Boolean,
    val linksRepository: LinksRepository,
    val hitsRepository: HitsRepository,
    val linksResourceItemStateHolder: LinksResourceItemStateHolder,
) : ViewModel(), LinksActions, LinksResourceItemStateHolder by linksResourceItemStateHolder {

    private val _state = MutableStateFlow(LinksScreenState.initial)
    val state = combine(
        _state,
        linksResourceItemStateHolder.items,
        linksResourceItemStateHolder.hits,
    ) { state, links, hits ->
        state.copy(
            links = links,
            hits = hits,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), LinksScreenState.initial)

    init {
        linksResourceItemStateHolder.init(viewModelScope, isUpcoming)

        val selectedLinksSortType = if (isUpcoming) LinksSortType.Active else LinksSortType.Newest
        val linksType = if (isUpcoming) LinksType.UPCOMING else LinksType.HOMEPAGE

        _state.update { previousState ->
            previousState.copy(
                isUpcoming = isUpcoming,
                sortMenuState = DropdownMenuState(
                    items = linksRepository.getLinksSortTypes(isUpcoming)
                        .map { linksSortType -> linksSortType.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = selectedLinksSortType.toDropdownMenuItemType(),
                    expanded = false
                )
            )
        }

        viewModelScope.launch {
            linksRepository.getLinks(
                page = 1,
                limit = null,
                linksSortType = selectedLinksSortType,
                linksType = linksType,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    linksResourceItemStateHolder.updateData(it.data)
                }
                .onFailure {
                    println("DBG --> failed to load links with $it")
                }

            if (!isUpcoming) {
                hitsRepository.getLinkHits(hitsSortType = HitsSortType.Day)
                    .onSuccess {
                        linksResourceItemStateHolder.updateHits(it.data)
                    }
                    .onFailure {
                        println("DBG --> failed to load hits with $it")
                    }
            }
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        _state.update { previousState ->
            previousState.updateSortMenuSelected(sortType)
        }
    }

    override fun onSortExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.updateSortMenuExpanded(expanded)
        }
    }

    override fun onSortDismissed() {
        _state.update { previousState ->
            previousState.updateSortMenuExpanded(false)
        }
    }

}
