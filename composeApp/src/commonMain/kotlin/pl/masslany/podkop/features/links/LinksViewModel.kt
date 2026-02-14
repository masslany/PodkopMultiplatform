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
import pl.masslany.podkop.common.models.toLinksSortType
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.features.topbar.TopBarActions

class LinksViewModel(
    val isUpcoming: Boolean,
    val linksRepository: LinksRepository,
    val hitsRepository: HitsRepository,
    val linksResourceItemStateHolder: LinksResourceItemStateHolder,
    topBarActions: TopBarActions,
) : ViewModel(),
    LinksActions,
    TopBarActions by topBarActions,
    LinksResourceItemStateHolder by linksResourceItemStateHolder {

    val linksType = if (isUpcoming) LinksType.UPCOMING else LinksType.HOMEPAGE
    var selectedLinksSortType = if (isUpcoming) LinksSortType.Active else LinksSortType.Newest

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            linksResourceItemStateHolder.appendData(data)
        },
    ) { request ->
        linksRepository.getLinks(
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            limit = null,
            linksType = linksType,
            linksSortType = selectedLinksSortType,
            bucket = null,
            category = null,
        )
    }

    private val _state = MutableStateFlow(LinksScreenState.initial)
    val state = combine(
        _state,
        linksResourceItemStateHolder.items,
        linksResourceItemStateHolder.hits,
        paginator.state,
    ) { state, links, hits, paginator ->
        state.copy(
            links = links,
            hits = hits,
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), LinksScreenState.initial)

    init {
        linksResourceItemStateHolder.init(viewModelScope, isUpcoming)

        _state.update { previousState ->
            previousState.copy(
                isUpcoming = isUpcoming,
                sortMenuState = DropdownMenuState(
                    items = linksRepository.getLinksSortTypes(isUpcoming)
                        .map { linksSortType -> linksSortType.toDropdownMenuItemType() }
                        .toImmutableList(),
                    selected = selectedLinksSortType.toDropdownMenuItemType(),
                    expanded = false,
                ),
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
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                    }
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
        selectedLinksSortType = sortType.toLinksSortType()

        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateRefreshing(true)
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
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    println("DBG --> failed to load links with $it")
                }
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

    override fun onRefresh(sortType: DropdownMenuItemType) {
        selectedLinksSortType = sortType.toLinksSortType()

        _state.update { previousState ->
            previousState
                .updateRefreshing(true)
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
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    println("DBG --> failed to load links with $it")
                }
        }
    }

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }
}
