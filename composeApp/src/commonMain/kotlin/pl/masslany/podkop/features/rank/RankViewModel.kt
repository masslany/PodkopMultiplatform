package pl.masslany.podkop.features.rank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.rank.domain.main.RankRepository
import pl.masslany.podkop.business.rank.domain.models.RankEntries
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.numberOrNull
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class RankViewModel(
    private val rankRepository: RankRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    RankActions,
    TopBarActions by topBarActions {

    private val rankItems = MutableStateFlow(emptyList<RankUserItemState>().toImmutableList())
    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            val newItems = data.map { it.toRankUserItemState() }
            rankItems.update { previousItems ->
                (previousItems + newItems).toImmutableList()
            }
        },
        onError = {
            logger.error("Failed to load paginated rank items", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        val page = request.numberOrNull() ?: run {
            logger.warn("Ignoring rank pagination request because numbered page was expected, got $request")
            return@Paginator Result.success(RankEntries(emptyList(), null))
        }

        rankRepository.getRank(
            page = page,
        )
    }

    private val _state = MutableStateFlow(RankScreenState.initial)
    val state = combine(
        _state,
        rankItems,
        paginator.state,
    ) { state, items, paginatorState ->
        state.copy(
            items = items.toPersistentList(),
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        RankScreenState.initial,
    )

    init {
        loadRank(showErrorScreenOnFailure = true)
    }

    override fun onRefresh() {
        _state.update { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
        }
        loadRank(showErrorScreenOnFailure = state.value.items.isEmpty())
    }

    override fun onUserClicked(username: String) {
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    fun paginate() {
        paginator.paginate()
    }

    private fun loadRank(showErrorScreenOnFailure: Boolean) {
        viewModelScope.launch {
            rankRepository.getRank(page = 1)
                .onSuccess {
                    rankItems.value = it.data.map { item -> item.toRankUserItemState() }.toImmutableList()
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load ranking", it)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(showErrorScreenOnFailure)
                            .updateRefreshing(false)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }
}
