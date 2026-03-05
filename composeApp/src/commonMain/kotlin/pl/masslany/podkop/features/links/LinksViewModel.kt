package pl.masslany.podkop.features.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.toDropdownMenuItemType
import pl.masslany.podkop.common.models.toLinksSortType
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class LinksViewModel(
    private val isUpcoming: Boolean,
    private val authRepository: AuthRepository,
    private val linksRepository: LinksRepository,
    private val hitsRepository: HitsRepository,
    private val linksResourceItemStateHolder: LinksResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    LinksActions,
    TopBarActions by topBarActions,
    LinksResourceItemStateHolder by linksResourceItemStateHolder {

    val linksType = if (isUpcoming) LinksType.UPCOMING else LinksType.HOMEPAGE
    var selectedLinksSortType = if (isUpcoming) LinksSortType.Active else LinksSortType.Newest
    private val screenInstanceId = Uuid.random().toString()

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            linksResourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated links", it)
            snackbarManager.tryEmitGenericError()
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

    private val _state = MutableStateFlow(
        LinksScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )
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
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        LinksScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

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
                page = resolveFirstPageParam(),
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
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load links", it)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(true)
                            .updateRefreshing(false)
                    }
                }

            if (!isUpcoming) {
                hitsRepository.getLinkHits(
                    hitsSortType = HitsSortType.Day,
                    year = null,
                    month = null,
                )
                    .onSuccess {
                        linksResourceItemStateHolder.updateHits(it.data)
                    }
                    .onFailure {
                        logger.error("Failed to load hits", it)
                    }
            }
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        selectedLinksSortType = sortType.toLinksSortType()

        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateError(false)
                .updateRefreshing(true)
        }
        viewModelScope.launch {
            linksRepository.getLinks(
                page = resolveFirstPageParam(),
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
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load links for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.links.isEmpty()
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                            .updateError(shouldShowErrorScreen)
                    }
                    snackbarManager.tryEmitGenericError()
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
                .updateError(false)
                .updateRefreshing(true)
        }
        viewModelScope.launch {
            linksRepository.getLinks(
                page = resolveFirstPageParam(),
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
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to refresh links for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.links.isEmpty()
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                            .updateError(shouldShowErrorScreen)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }

    private suspend fun resolveFirstPageParam(): Any? = if (authRepository.isLoggedIn()) {
        null
    } else {
        1
    }
}
