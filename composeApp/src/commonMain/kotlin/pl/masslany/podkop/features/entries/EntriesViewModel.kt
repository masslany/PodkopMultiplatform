package pl.masslany.podkop.features.entries

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.models.toDropdownMenuItemType
import pl.masslany.podkop.common.models.toEntriesSortType
import pl.masslany.podkop.common.models.toHotSortType
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class EntriesViewModel(
    private val authRepository: AuthRepository,
    private val entriesRepository: EntriesRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntriesActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentEntriesSortType: EntriesSortType = EntriesSortType.Hot
    private var currentHotSortType: HotSortType = HotSortType.TwelveHours
    private val screenInstanceId = Uuid.random().toString()

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

    private val _state = MutableStateFlow(
        EntriesScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

    // TODO: Think of better UI events system
    private val _entryCreatedNavigation = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val entryCreatedNavigation: SharedFlow<Int> = _entryCreatedNavigation.asSharedFlow()

    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, entries, paginator ->
        state.copy(
            entries = entries,
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        EntriesScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

    private val entriesSortTypes = entriesRepository.getEntriesSortTypes()
        .map { entriesSortType -> entriesSortType.toDropdownMenuItemType() }
        .toImmutableList()

    private val hotSortTypes = entriesRepository.getHotSortTypes()
        .map { hotSortType -> hotSortType.toDropdownMenuItemType() }
        .toImmutableList()

    init {
        resourceItemStateHolder.init(viewModelScope)

        _state.update { previousState ->
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
            _state.update { previousState ->
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
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load entries", it)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(true)
                            .updateRefreshing(false)
                    }
                }
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        currentEntriesSortType = sortType.toEntriesSortType()
        currentHotSortType = HotSortType.TwelveHours

        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType, hotSortTypes)
                .updateError(false)
                .updateRefreshing(true)
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
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
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

    override fun onHotSortSelected(sortType: DropdownMenuItemType) {
        currentEntriesSortType = EntriesSortType.Hot
        currentHotSortType = sortType.toHotSortType()

        _state.update { previousState ->
            previousState
                .updateHotSortMenuSelected(sortType)
                .updateError(false)
                .updateRefreshing(true)
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
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load hot entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
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

    override fun onHotSortExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.updateHotSortMenuExpanded(expanded)
        }
    }

    override fun onHotSortDismissed() {
        _state.update { previousState ->
            previousState.updateHotSortMenuExpanded(false)
        }
    }

    override fun onRefresh(sortType: DropdownMenuItemType) {
        _state.update { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
        }
        viewModelScope.launch {
            entriesRepository.getEntries(
                page = resolveFirstPageParam(),
                limit = null,
                entriesSortType = sortType.toEntriesSortType(),
                hotSortType = HotSortType.TwelveHours,
                category = null,
                bucket = null,
            )
                .onSuccess {
                    resourceItemStateHolder.updateData(it.data)
                    paginator.setup(it.pagination, it.data.size)
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(false)
                            .updateRefreshing(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to refresh entries for sort type $sortType", it)
                    val shouldShowErrorScreen = state.value.entries.isEmpty()
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

    override fun onTopBarAddEntryClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                return@launch
            }
            _state.update { previousState ->
                previousState.copy(
                    isComposerVisible = true,
                    composerContent = TextFieldValue(),
                    composerAdult = false,
                    isComposerSubmitting = false,
                )
            }
        }
    }

    override fun onComposerTextChanged(content: TextFieldValue) {
        _state.update { previousState ->
            previousState.copy(composerContent = content)
        }
    }

    override fun onComposerAdultChanged(adult: Boolean) {
        _state.update { previousState ->
            previousState.copy(composerAdult = adult)
        }
    }

    override fun onComposerDismissed() {
        _state.update { previousState ->
            previousState.copy(
                isComposerVisible = false,
                composerContent = TextFieldValue(),
                composerAdult = false,
                isComposerSubmitting = false,
            )
        }
    }

    override fun onComposerSubmit() {
        val currentState = state.value
        if (!currentState.isComposerVisible || currentState.isComposerSubmitting) {
            return
        }

        val content = currentState.composerContent.text.trim()
        if (content.isBlank()) {
            return
        }

        _state.update { previousState ->
            previousState.copy(isComposerSubmitting = true)
        }

        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _state.update { previousState ->
                    previousState.copy(isComposerSubmitting = false)
                }
                return@launch
            }

            entriesRepository.createEntry(
                content = content,
                adult = currentState.composerAdult,
            ).onSuccess { createdEntry ->
                _state.update { previousState ->
                    previousState.copy(
                        isComposerVisible = false,
                        composerContent = TextFieldValue(),
                        composerAdult = false,
                        isComposerSubmitting = false,
                    )
                }
                _entryCreatedNavigation.tryEmit(createdEntry.id)
            }.onFailure {
                logger.error("Failed to create entry from entries composer", it)
                _state.update { previousState ->
                    previousState.copy(isComposerSubmitting = false)
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
