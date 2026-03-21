package pl.masslany.podkop.features.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.toDropdownMenuItemType
import pl.masslany.podkop.common.models.toEntriesSortType
import pl.masslany.podkop.common.models.toHotSortType
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerRequest
import pl.masslany.podkop.features.composer.ComposerResult
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class EntriesViewModel(
    private val authRepository: AuthRepository,
    private val entriesRepository: EntriesRepository,
    private val notificationsRepository: NotificationsRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntriesActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentEntriesSortType: EntriesSortType = EntriesSortType.Hot
    private var currentHotSortType: HotSortType = HotSortType.TwelveHours
    private val screenInstanceId = Uuid.random().toString()
    private var lastOpenedAt: Instant? = null
    private var hasBeenInitialized = false

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated entries", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        entriesRepository.getEntries(
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            limit = null,
            entriesSortType = currentEntriesSortType,
            hotSortType = currentHotSortType,
            category = null,
            bucket = null,
        )
    }

    private val _state = MutableStateFlow(initialState())

    // TODO: Think of better UI events system
    private val _entryCreatedNavigation = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val entryCreatedNavigation: SharedFlow<Int> = _entryCreatedNavigation.asSharedFlow()

    val state = combine(
        _state,
        resourceItemStateHolder.items,
        notificationsRepository.unreadCount,
        paginator.state,
    ) { state, entries, notificationsUnreadCount, paginatorState ->
        state.copy(
            entries = entries,
            notificationsUnreadCount = notificationsUnreadCount,
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        initialState(),
    )

    private val entriesSortTypes = entriesRepository.getEntriesSortTypes()
        .map { entriesSortType -> entriesSortType.toDropdownMenuItemType() }
        .toImmutableList()

    private val hotSortTypes = entriesRepository.getHotSortTypes()
        .map { hotSortType -> hotSortType.toDropdownMenuItemType() }
        .toImmutableList()

    init {
        resourceItemStateHolder.init(viewModelScope)

        updateState { previousState ->
            previousState.copy(
                sortMenuState = DropdownMenuState(
                    items = entriesSortTypes,
                    selected = DropdownMenuItemType.Hot,
                    expanded = false,
                ),
                hotSortMenuState = DropdownMenuState(
                    items = hotSortTypes,
                    selected = DropdownMenuItemType.TwelveHours,
                    expanded = false,
                ),
            )
        }

        viewModelScope.launch {
            updateState { previousState ->
                previousState.copy(
                    isLoggedIn = authRepository.isLoggedIn(),
                )
            }
            entriesRepository.getEntries(
                page = resolveFirstPageParam(),
                limit = null,
                entriesSortType = currentEntriesSortType,
                hotSortType = currentHotSortType,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load entries", it)
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(true)
                            .updateRefreshing(false)
                    }
                }
            hasBeenInitialized = true
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        currentEntriesSortType = sortType.toEntriesSortType()
        currentHotSortType = HotSortType.TwelveHours

        updateState { previousState ->
            previousState
                .updateSortMenuSelected(sortType, hotSortTypes)
                .updateError(false)
                .updateRefreshing(true)
                .updateRefreshPromptVisible(false)
        }
        viewModelScope.launch {
            entriesRepository.getEntries(
                page = resolveFirstPageParam(),
                limit = null,
                entriesSortType = sortType.toEntriesSortType(),
                hotSortType = currentHotSortType,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
                    updateState { previousState ->
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
        updateState { previousState ->
            previousState.updateSortMenuExpanded(expanded)
        }
    }

    override fun onSortDismissed() {
        updateState { previousState ->
            previousState.updateSortMenuExpanded(false)
        }
    }

    override fun onHotSortSelected(sortType: DropdownMenuItemType) {
        currentEntriesSortType = EntriesSortType.Hot
        currentHotSortType = sortType.toHotSortType()

        updateState { previousState ->
            previousState
                .updateHotSortMenuSelected(sortType)
                .updateError(false)
                .updateRefreshing(true)
                .updateRefreshPromptVisible(false)
        }
        viewModelScope.launch {
            entriesRepository.getEntries(
                page = resolveFirstPageParam(),
                limit = null,
                entriesSortType = currentEntriesSortType,
                hotSortType = sortType.toHotSortType(),
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load hot entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                            .updateError(shouldShowErrorScreen)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    override fun onHotSortExpandedChanged(expanded: Boolean) {
        updateState { previousState ->
            previousState.updateHotSortMenuExpanded(expanded)
        }
    }

    override fun onHotSortDismissed() {
        updateState { previousState ->
            previousState.updateHotSortMenuExpanded(false)
        }
    }

    override fun onRefresh(sortType: DropdownMenuItemType) {
        updateState { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
                .updateRefreshPromptVisible(false)
        }
        viewModelScope.launch {
            entriesRepository.getEntries(
                page = resolveFirstPageParam(),
                limit = null,
                entriesSortType = currentEntriesSortType,
                hotSortType = currentHotSortType,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to refresh entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
                    updateState { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateRefreshing(false)
                            .updateError(shouldShowErrorScreen)
                    }
                    snackbarManager.tryEmitGenericError()
                }
            hasBeenInitialized = true
        }
    }

    override fun onRefreshPromptDismissed() {
        updateState { previousState ->
            previousState.updateRefreshPromptVisible(false)
        }
    }

    override fun onTopBarAddEntryClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                return@launch
            }

            val resultKey = "entries-composer-${Uuid.random()}"
            val result = appNavigator.awaitResult<ComposerResult>(
                target = ComposerBottomSheetScreen(
                    resultKey = resultKey,
                    request = ComposerRequest.CreateEntry(),
                ),
                key = resultKey,
            )

            if (result is ComposerResult.Submitted) {
                _entryCreatedNavigation.tryEmit(result.resource.id)
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

    fun onScreenOpened() {
        val previousOpenedAt = lastOpenedAt
        val now = Clock.System.now()
        lastOpenedAt = now

        val shouldShowRefreshPrompt = hasBeenInitialized &&
            previousOpenedAt != null &&
            now - previousOpenedAt > STALE_REFRESH_THRESHOLD

        updateState { previousState ->
            previousState.updateRefreshPromptVisible(shouldShowRefreshPrompt)
        }
    }

    private suspend fun resolveFirstPageParam(): Any? = if (authRepository.isLoggedIn()) {
        null
    } else {
        1
    }

    private inline fun updateState(transform: (EntriesScreenState) -> EntriesScreenState) {
        _state.update { previousState ->
            transform(previousState)
        }
    }

    private fun initialState(): EntriesScreenState = EntriesScreenState.initial.copy(
        screenInstanceId = screenInstanceId,
    )

    private companion object {
        val STALE_REFRESH_THRESHOLD = 3.hours
    }
}
