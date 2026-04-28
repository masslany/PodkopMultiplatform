package pl.masslany.podkop.features.blacklists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.navigation.dialogText
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.requireNumber
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionsState
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionsStatus
import pl.masslany.podkop.features.blacklists.models.BlacklistedTagSuggestionItemState
import pl.masslany.podkop.features.blacklists.models.BlacklistedUserSuggestionItemState
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.topbar.TopBarActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.blacklists_button_remove
import podkop.composeapp.generated.resources.blacklists_dialog_remove_title
import podkop.composeapp.generated.resources.dialog_button_dismiss

class BlacklistsViewModel(
    private val blacklistsRepository: BlacklistsRepository,
    private val profileRepository: ProfileRepository,
    private val tagsRepository: TagsRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    BlacklistsActions,
    TopBarActions by topBarActions {
    private val _state = MutableStateFlow(BlacklistsScreenState.initial)
    private val usersController = CategoryController(BlacklistCategoryType.Users)
    private val tagsController = CategoryController(BlacklistCategoryType.Tags)
    private val domainsController = CategoryController(BlacklistCategoryType.Domains)
    private val categoryControllers = mapOf(
        BlacklistCategoryType.Users to usersController,
        BlacklistCategoryType.Tags to tagsController,
        BlacklistCategoryType.Domains to domainsController,
    )

    val state = combine(
        _state,
        usersController.state,
        tagsController.state,
        domainsController.state,
    ) { state, usersState, tagsState, domainsState ->
        state.copy(
            categories = persistentListOf(
                usersState,
                tagsState,
                domainsState,
            ),
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        BlacklistsScreenState.initial,
    )

    init {
        loadAllCategories(isRefreshing = false)
    }

    override fun onCategorySelected(category: BlacklistCategoryType) {
        _state.update { previousState ->
            previousState.copy(selectedCategory = category)
        }
    }

    override fun onRefresh() {
        loadAllCategories(isRefreshing = true)
    }

    override fun onAddInputChanged(value: String) {
        categoryControllers.getValue(state.value.selectedCategory).updateInput(value)
    }

    override fun onAddClicked() {
        viewModelScope.launch {
            categoryControllers.getValue(state.value.selectedCategory).submitCurrentInput()
        }
    }

    override fun onSuggestionClicked(value: String) {
        viewModelScope.launch {
            categoryControllers.getValue(state.value.selectedCategory).submit(value)
        }
    }

    override fun onRetrySuggestionsClicked() {
        viewModelScope.launch {
            categoryControllers.getValue(state.value.selectedCategory).retrySuggestions()
        }
    }

    override fun onEntryClicked(item: BlacklistEntryState) {
        when (item) {
            is BlacklistEntryState.BlacklistedUserItemState -> appNavigator.navigateTo(ProfileScreen(username = item.username))
            is BlacklistEntryState.BlacklistedTagItemState -> appNavigator.navigateTo(TagScreen(tag = item.name))
            else -> Unit
        }
    }

    override fun onRemoveClicked(item: BlacklistEntryState) {
        viewModelScope.launch {
            val dialog = GenericDialog(
                title = dialogText(Res.string.blacklists_dialog_remove_title, item.displayLabel),
                positiveText = dialogText(Res.string.blacklists_button_remove),
                negativeText = dialogText(Res.string.dialog_button_dismiss),
            )
            val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
            if (!confirmed) return@launch

            categoryControllers.getValue(item.category).remove(item)
        }
    }

    fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = categoryControllers.getValue(state.value.selectedCategory)
        .paginator
        .shouldPaginate(lastVisibleIndex, totalItems, PAGINATION_PREFETCH_DISTANCE)

    fun paginate() {
        categoryControllers.getValue(state.value.selectedCategory).paginator.paginate()
    }

    private fun loadAllCategories(isRefreshing: Boolean) {
        viewModelScope.launch {
            if (isRefreshing) {
                _state.update { previousState ->
                    previousState.copy(isRefreshing = true)
                }
            }

            val hadFailure = supervisorScope {
                categoryControllers.values
                    .map { controller ->
                        async {
                            controller.loadFirstPage(
                                showErrorScreenOnFailure = controller.hasNoItems(),
                                isRefreshing = isRefreshing,
                            ).isFailure
                        }
                    }
                    .any { deferred -> deferred.await() }
            }

            if (hadFailure) {
                snackbarManager.tryEmitGenericError()
            }

            if (isRefreshing) {
                _state.update { previousState ->
                    previousState.copy(isRefreshing = false)
                }
            }
        }
    }

    private inner class CategoryController(private val type: BlacklistCategoryType) {
        private val _state = MutableStateFlow(BlacklistCategoryState.initial(type))

        val paginator = Paginator(
            scope = viewModelScope,
            onNewItems = { data ->
                _state.update { previousState ->
                    previousState.copy(
                        items = (previousState.items + data).toPersistentList(),
                    )
                }
            },
            onError = { throwable ->
                logger.error("Failed to paginate blacklist items for $type", throwable)
                snackbarManager.tryEmitGenericError()
            },
        ) { request ->
            loadPage(
                page = request.requireNumber(),
            )
        }

        init {
            observeSuggestions()
        }

        val state = combine(this@CategoryController._state, paginator.state) { currentState, paginatorState ->
            currentState.copy(
                isPaginating = paginatorState is PaginatorState.Loading,
                canSubmit = normalizeInput(currentState.addInput).isNotBlank() &&
                    !currentState.isActionsInProgress,
            )
        }.stateIn(
            viewModelScope,
            WhileSubscribed(5000),
            BlacklistCategoryState.initial(type),
        )

        fun hasNoItems(): Boolean = _state.value.items.isEmpty()

        fun updateInput(value: String) {
            _state.update { previousState ->
                previousState.copy(addInput = value)
            }
        }

        suspend fun retrySuggestions() {
            loadSuggestions(_state.value.addInput)
        }

        suspend fun submitCurrentInput() {
            submit(_state.value.addInput)
        }

        suspend fun submit(value: String) {
            val normalizedValue = normalizeInput(value)
            if (normalizedValue.isBlank() || _state.value.isActionsInProgress) {
                return
            }

            _state.update { previousState ->
                previousState.copy(isActionsInProgress = true)
            }
            val action = when (type) {
                BlacklistCategoryType.Users -> blacklistsRepository.addBlacklistedUser(normalizedValue)
                BlacklistCategoryType.Tags -> blacklistsRepository.addBlacklistedTag(normalizedValue)
                BlacklistCategoryType.Domains -> blacklistsRepository.addBlacklistedDomain(normalizedValue)
            }

            if (action.isSuccess) {
                _state.update { previousState ->
                    previousState.copy(
                        addInput = "",
                        suggestions = BlacklistSuggestionsState.initial,
                    )
                }
                refreshAfterMutation()
            } else {
                val throwable = action.exceptionOrNull()
                if (throwable != null) {
                    logger.error("Failed to add blacklist item for $type", throwable)
                }
                snackbarManager.tryEmitGenericError()
            }
            _state.update { previousState ->
                previousState.copy(isActionsInProgress = false)
            }
        }

        suspend fun remove(item: BlacklistEntryState) {
            if (_state.value.isActionsInProgress) {
                return
            }

            _state.update { previousState ->
                previousState.copy(isActionsInProgress = true)
            }
            val action = when (item) {
                is BlacklistEntryState.BlacklistedUserItemState -> blacklistsRepository.removeBlacklistedUser(item.username)
                is BlacklistEntryState.BlacklistedTagItemState -> blacklistsRepository.removeBlacklistedTag(item.name)
                is BlacklistEntryState.BlacklistedDomainItemState -> blacklistsRepository.removeBlacklistedDomain(item.domain)
            }

            if (action.isSuccess) {
                _state.update { previousState ->
                    previousState.copy(
                        items = previousState.items
                            .filterNot { currentItem -> currentItem.key == item.key }
                            .toPersistentList(),
                        totalCount = (previousState.totalCount - 1).coerceAtLeast(0),
                    )
                }
                paginator.onItemsRemoved()
            } else {
                val throwable = action.exceptionOrNull()
                if (throwable != null) {
                    logger.error("Failed to remove blacklist item for $type", throwable)
                }
                snackbarManager.tryEmitGenericError()
            }
            _state.update { previousState ->
                previousState.copy(isActionsInProgress = false)
            }
        }

        suspend fun loadFirstPage(
            showErrorScreenOnFailure: Boolean,
            isRefreshing: Boolean,
        ): Result<Unit> {
            if (!isRefreshing || _state.value.items.isEmpty()) {
                _state.update { previousState ->
                    previousState.copy(isLoading = true)
                }
            }
            _state.update { previousState ->
                previousState.copy(isError = false)
            }

            return loadPage(page = 1)
                .onSuccess { page ->
                    _state.update { previousState ->
                        previousState.copy(
                            totalCount = page.pagination?.total ?: page.data.size,
                            isLoading = false,
                            isError = false,
                            items = page.data.toPersistentList(),
                        )
                    }
                    paginator.setup(page.pagination, page.data.size)
                }
                .onFailure { throwable ->
                    logger.error("Failed to load blacklist items for $type", throwable)
                    _state.update { previousState ->
                        previousState.copy(
                            isLoading = false,
                            isError = showErrorScreenOnFailure,
                        )
                    }
                }
                .map { /* Unit */ }
        }

        private suspend fun refreshAfterMutation() {
            loadFirstPage(
                showErrorScreenOnFailure = _state.value.items.isEmpty(),
                isRefreshing = true,
            ).onFailure {
                snackbarManager.tryEmitGenericError()
            }
        }

        @OptIn(FlowPreview::class)
        private fun observeSuggestions() {
            if (!type.supportsSuggestions) {
                return
            }

            viewModelScope.launch {
                _state
                    .map { controllerState -> controllerState.addInput }
                    .debounce(SUGGESTION_DEBOUNCE_MILLIS)
                    .distinctUntilChanged()
                    .collectLatest { query ->
                        loadSuggestions(query)
                    }
            }
        }

        private suspend fun loadSuggestions(query: String) {
            val normalizedQuery = normalizeInput(query)
            if (!type.supportsSuggestions || normalizedQuery.length < MIN_SUGGESTION_QUERY_LENGTH) {
                _state.update { previousState ->
                    previousState.copy(suggestions = BlacklistSuggestionsState.initial)
                }
                return
            }

            _state.update { previousState ->
                previousState.copy(
                    suggestions = BlacklistSuggestionsState(
                        status = BlacklistSuggestionsStatus.Loading,
                        items = persistentListOf(),
                    ),
                )
            }

            when (type) {
                BlacklistCategoryType.Users -> {
                    profileRepository.getUsersAutoComplete(query = normalizedQuery)
                        .onSuccess { autocomplete ->
                            if (normalizeInput(_state.value.addInput) != normalizedQuery) {
                                return
                            }

                            _state.update { previousState ->
                                previousState.copy(
                                    suggestions = BlacklistSuggestionsState(
                                        status = BlacklistSuggestionsStatus.Content,
                                        items = autocomplete.users
                                            .map { item -> item.toSuggestionItemState() }
                                            .filterNot(::isUserAlreadyBlacklisted)
                                            .distinctBy(BlacklistedUserSuggestionItemState::username)
                                            .toPersistentList(),
                                    ),
                                )
                            }
                        }
                        .onFailure { throwable ->
                            if (normalizeInput(_state.value.addInput) != normalizedQuery) {
                                return
                            }

                            logger.error("Failed to load blacklist user suggestions", throwable)
                            _state.update { previousState ->
                                previousState.copy(
                                    suggestions = BlacklistSuggestionsState(
                                        status = BlacklistSuggestionsStatus.Error,
                                        items = persistentListOf(),
                                    ),
                                )
                            }
                        }
                }

                BlacklistCategoryType.Tags -> {
                    tagsRepository.getAutoCompleteTags(query = normalizedQuery)
                        .onSuccess { autocomplete ->
                            if (normalizeInput(_state.value.addInput) != normalizedQuery) {
                                return
                            }

                            _state.update { previousState ->
                                previousState.copy(
                                    suggestions = BlacklistSuggestionsState(
                                        status = BlacklistSuggestionsStatus.Content,
                                        items = autocomplete.tags
                                            .map { item -> item.toSuggestionItemState() }
                                            .filterNot(::isTagAlreadyBlacklisted)
                                            .distinctBy(BlacklistedTagSuggestionItemState::name)
                                            .toPersistentList(),
                                    ),
                                )
                            }
                        }
                        .onFailure { throwable ->
                            if (normalizeInput(_state.value.addInput) != normalizedQuery) {
                                return
                            }

                            logger.error("Failed to load blacklist tag suggestions", throwable)
                            _state.update { previousState ->
                                previousState.copy(
                                    suggestions = BlacklistSuggestionsState(
                                        status = BlacklistSuggestionsStatus.Error,
                                        items = persistentListOf(),
                                    ),
                                )
                            }
                        }
                }

                BlacklistCategoryType.Domains -> Unit
            }
        }

        private fun isUserAlreadyBlacklisted(suggestion: BlacklistedUserSuggestionItemState): Boolean =
            _state.value.items.any { item ->
                item is BlacklistEntryState.BlacklistedUserItemState && item.username == suggestion.username
            }

        private fun isTagAlreadyBlacklisted(suggestion: BlacklistedTagSuggestionItemState): Boolean =
            _state.value.items.any { item ->
                item is BlacklistEntryState.BlacklistedTagItemState && item.name == normalizeTag(suggestion.name)
            }

        private fun normalizeInput(value: String): String = when (type) {
            BlacklistCategoryType.Users -> value.trim().removePrefix("@")
            BlacklistCategoryType.Tags -> normalizeTag(value)
            BlacklistCategoryType.Domains -> value.trim().lowercase()
        }

        private suspend fun loadPage(page: Int): Result<PaginatedData<BlacklistEntryState>> =
            when (type) {
                BlacklistCategoryType.Users -> blacklistsRepository.getBlacklistedUsers(page = page)
                    .map { response ->
                        BlacklistPage(
                            data = response.data.map { item -> item.toItemState() },
                            pagination = response.pagination,
                        )
                    }

                BlacklistCategoryType.Tags -> blacklistsRepository.getBlacklistedTags(page = page)
                    .map { response ->
                        BlacklistPage(
                            data = response.data.map { item -> item.toItemState() },
                            pagination = response.pagination,
                        )
                    }

                BlacklistCategoryType.Domains -> blacklistsRepository.getBlacklistedDomains(page = page)
                    .map { response ->
                        BlacklistPage(
                            data = response.data.map { item -> item.toItemState() },
                            pagination = response.pagination,
                        )
                    }
            }
    }

    private data class BlacklistPage(
        override val data: List<BlacklistEntryState>,
        override val pagination: Pagination?,
    ) : PaginatedData<BlacklistEntryState>

    private companion object {
        const val PAGINATION_PREFETCH_DISTANCE = 6
        const val MIN_SUGGESTION_QUERY_LENGTH = 3
        const val SUGGESTION_DEBOUNCE_MILLIS = 300L
    }
}

private val BlacklistCategoryType.supportsSuggestions: Boolean
    get() = this == BlacklistCategoryType.Users || this == BlacklistCategoryType.Tags

private fun normalizeTag(value: String): String = value
    .trim()
    .removePrefix("#")
    .lowercase()
