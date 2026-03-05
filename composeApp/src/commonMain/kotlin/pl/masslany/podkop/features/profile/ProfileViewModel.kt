package pl.masslany.podkop.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.toPage
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileListItem
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import pl.masslany.podkop.features.profile.models.isObservedTags
import pl.masslany.podkop.features.profile.models.isObservedUsers
import pl.masslany.podkop.features.profile.models.isResourceBacked
import pl.masslany.podkop.features.profile.models.toSubActionItems
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.topbar.TopBarActions

class ProfileViewModel(
    private val username: String,
    private val profileRepository: ProfileRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    ProfileActions,
    TopBarActions by topBarActions,
    ResourceItemActions by resourceItemStateHolder {

    private var selectedSubActionType: ProfileSubActionType = ProfileSubActionType.Actions
    private var paginatedSubActionType: ProfileSubActionType? = null
    private var resourcesLoadRequestId: Int = 0
    private val listCache = mutableMapOf<ProfileSubActionType, CachedProfileItems>()
    private val listOwner = MutableStateFlow<ProfileSubActionType?>(null)
    private val observedListContent = MutableStateFlow<ProfileListContentState>(ProfileListContentState.Empty)

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { newItems ->
            val targetSubAction = paginatedSubActionType ?: selectedSubActionType
            val previousCache = listCache[targetSubAction]
            val mergedItems = previousCache
                ?.items
                .orEmpty()
                .appendDistinct(newItems)

            listCache[targetSubAction] = CachedProfileItems(
                items = mergedItems,
                pagination = previousCache?.pagination,
            )

            if (targetSubAction == selectedSubActionType) {
                presentItems(
                    subActionType = targetSubAction,
                    items = mergedItems,
                )
            }
        },
        onError = {
            logger.error("Failed to load paginated profile list for $selectedSubActionType", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        val page = request.toPage()
            ?: return@Paginator Result.failure(
                IllegalArgumentException("Unsupported profile pagination request: $request"),
            )
        val targetSubAction = paginatedSubActionType ?: selectedSubActionType

        profileRepository.getProfileListItems(
            username = username,
            subActionType = targetSubAction,
            page = page,
        ).onSuccess { response ->
            val previousCache = listCache[targetSubAction]
            listCache[targetSubAction] = CachedProfileItems(
                items = previousCache?.items.orEmpty(),
                pagination = response.pagination,
            )
        }
    }

    private val _state = MutableStateFlow(ProfileScreenState.initial.copy(username = username))
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
        listOwner,
        observedListContent,
    ) { state, resources, paginatorState, owner, observedContent ->
        if (paginatorState !is PaginatorState.Loading) {
            paginatedSubActionType = null
        }

        state.copy(
            listContent = resolveVisibleListContent(
                selectedSubAction = state.subActionState.selected,
                owner = owner,
                resources = resources,
                observedContent = observedContent,
            ),
            isPaginating = paginatorState is PaginatorState.Loading &&
                paginatedSubActionType == selectedSubActionType,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = ProfileScreenState.initial.copy(username = username),
        )

    init {
        resourceItemStateHolder.init(viewModelScope)
        viewModelScope.launch {
            loadData()
        }
    }

    override fun onRetryClicked() {
        viewModelScope.launch {
            loadData()
        }
    }

    override fun onSummarySelected(type: ProfileSummaryType) {
        val currentSummaryType = _state.value.selectedSummaryType
        val subActionItems = type.toSubActionItems()
        val selectedSubAction = subActionItems.firstOrNull() ?: return
        if (currentSummaryType == type) return

        _state.update { previousState ->
            previousState.copy(
                selectedSummaryType = type,
                subActionState = ProfileSubActionState(
                    items = subActionItems,
                    selected = selectedSubAction,
                    expanded = false,
                ),
            )
        }

        viewModelScope.launch {
            selectedSubActionType = selectedSubAction
            loadItemsForSubAction(
                subActionType = selectedSubAction,
                forceNetwork = false,
            )
        }
    }

    override fun onSubActionExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.copy(
                subActionState = previousState.subActionState.copy(expanded = expanded),
            )
        }
    }

    override fun onSubActionDismissed() {
        _state.update { previousState ->
            previousState.copy(
                subActionState = previousState.subActionState.copy(expanded = false),
            )
        }
    }

    override fun onSubActionSelected(type: ProfileSubActionType) {
        val currentSubActionState = _state.value.subActionState
        if (type !in currentSubActionState.items) return

        _state.update { previousState ->
            previousState.copy(
                subActionState = previousState.subActionState.copy(
                    selected = type,
                    expanded = false,
                ),
            )
        }

        if (type == selectedSubActionType) return

        viewModelScope.launch {
            selectedSubActionType = type
            loadItemsForSubAction(
                subActionType = type,
                forceNetwork = false,
            )
        }
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        if (_state.value.isResourcesLoading) return

        paginatedSubActionType = selectedSubActionType
        paginator.paginate()
    }

    private suspend fun loadData() {
        listCache.clear()
        resourcesLoadRequestId = 0
        selectedSubActionType = ProfileSubActionType.Actions
        paginatedSubActionType = null
        listOwner.value = null
        observedListContent.value = ProfileListContentState.Empty
        resourceItemStateHolder.updateData(emptyList())

        _state.update { previousState ->
            previousState.copy(
                isLoading = true,
                isError = false,
                isResourcesLoading = false,
                isPaginating = false,
                header = null,
                summary = persistentListOf(),
                selectedSummaryType = ProfileSummaryType.Actions,
                subActionState = ProfileSubActionState(
                    items = persistentListOf(ProfileSubActionType.Actions),
                    selected = ProfileSubActionType.Actions,
                    expanded = false,
                ),
                listContent = ProfileListContentState.Empty,
            )
        }

        profileRepository.getProfile(username)
            .onSuccess { profile ->
                val selectedSummaryType = ProfileSummaryType.Actions
                val subActionItems = selectedSummaryType.toSubActionItems()
                val selectedSubAction = subActionItems.first()
                selectedSubActionType = selectedSubAction

                _state.update { previousState ->
                    previousState.copy(
                        isLoading = false,
                        isError = false,
                        isResourcesLoading = false,
                        header = profile.toProfileHeaderState(),
                        summary = profile.summary.toSummaryItems(),
                        selectedSummaryType = selectedSummaryType,
                        subActionState = ProfileSubActionState(
                            items = subActionItems,
                            selected = selectedSubAction,
                            expanded = false,
                        ),
                    )
                }

                loadItemsForSubAction(
                    subActionType = selectedSubAction,
                    forceNetwork = true,
                )
            }
            .onFailure {
                listOwner.value = null
                observedListContent.value = ProfileListContentState.Empty
                resourceItemStateHolder.updateData(emptyList())
                _state.update { previousState ->
                    previousState.copy(
                        isLoading = false,
                        isError = true,
                        isResourcesLoading = false,
                        header = null,
                        summary = persistentListOf(),
                        selectedSummaryType = ProfileSummaryType.Actions,
                        subActionState = ProfileSubActionState(
                            items = persistentListOf(ProfileSubActionType.Actions),
                            selected = ProfileSubActionType.Actions,
                            expanded = false,
                        ),
                        listContent = ProfileListContentState.Empty,
                    )
                }
                logger.error("Failed to load profile", it)
            }
    }

    private suspend fun loadItemsForSubAction(
        subActionType: ProfileSubActionType,
        forceNetwork: Boolean,
    ) {
        val requestId = ++resourcesLoadRequestId

        if (!forceNetwork) {
            listCache[subActionType]?.let { cachedItems ->
                presentItems(
                    subActionType = subActionType,
                    items = cachedItems.items,
                )
                paginator.setup(cachedItems.pagination, cachedItems.items.size)
                listOwner.value = subActionType
                _state.update { previousState ->
                    previousState.copy(
                        isResourcesLoading = false,
                    )
                }
                return
            }
        }

        _state.update { previousState ->
            previousState.copy(isResourcesLoading = true)
        }
        listOwner.value = null
        paginator.setup(null, 0)

        profileRepository.getProfileListItems(
            username = username,
            subActionType = subActionType,
            page = 1,
        ).onSuccess { response ->
            listCache[subActionType] = CachedProfileItems(
                items = response.data,
                pagination = response.pagination,
            )

            if (requestId != resourcesLoadRequestId || subActionType != selectedSubActionType) {
                return@onSuccess
            }

            presentItems(
                subActionType = subActionType,
                items = response.data,
            )
            paginator.setup(response.pagination, response.data.size)
            listOwner.value = subActionType
            _state.update { previousState ->
                previousState.copy(
                    isResourcesLoading = false,
                )
            }
        }.onFailure {
            if (requestId != resourcesLoadRequestId || subActionType != selectedSubActionType) {
                return@onFailure
            }

            presentItems(
                subActionType = subActionType,
                items = emptyList(),
            )
            paginator.setup(null, 0)
            listOwner.value = subActionType
            _state.update { previousState ->
                previousState.copy(
                    isResourcesLoading = false,
                )
            }
            logger.error("Failed to load profile resources for $subActionType", it)
        }
    }

    private suspend fun presentItems(
        subActionType: ProfileSubActionType,
        items: List<ProfileListItem>,
    ) {
        when {
            subActionType.isResourceBacked() -> {
                resourceItemStateHolder.updateData(
                    items.mapNotNull { listItem ->
                        (listItem as? ProfileListItem.Resource)?.item
                    },
                )
                observedListContent.value = ProfileListContentState.Empty
            }

            subActionType.isObservedUsers() -> {
                resourceItemStateHolder.updateData(emptyList())
                observedListContent.value = ProfileListContentState.ObservedUsers(
                    items.mapNotNull { listItem ->
                        (listItem as? ProfileListItem.ObservedUserItem)?.user?.toItemState()
                    }.toImmutableList(),
                )
            }

            subActionType.isObservedTags() -> {
                resourceItemStateHolder.updateData(emptyList())
                observedListContent.value = ProfileListContentState.ObservedTags(
                    items.mapNotNull { listItem ->
                        (listItem as? ProfileListItem.ObservedTagItem)?.tag?.toItemState()
                    }.toImmutableList(),
                )
            }
        }
    }

    private fun resolveVisibleListContent(
        selectedSubAction: ProfileSubActionType?,
        owner: ProfileSubActionType?,
        resources: ImmutableList<ResourceItemState>,
        observedContent: ProfileListContentState,
    ): ProfileListContentState {
        if (selectedSubAction == null || owner != selectedSubAction) {
            return ProfileListContentState.Empty
        }

        return when {
            selectedSubAction.isResourceBacked() -> ProfileListContentState.Resources(resources)

            selectedSubAction.isObservedUsers() -> {
                observedContent as? ProfileListContentState.ObservedUsers ?:
                    ProfileListContentState.ObservedUsers(persistentListOf())
            }

            selectedSubAction.isObservedTags() -> {
                observedContent as? ProfileListContentState.ObservedTags ?:
                    ProfileListContentState.ObservedTags(persistentListOf())
            }

            else -> ProfileListContentState.Empty
        }
    }

    private data class CachedProfileItems(val items: List<ProfileListItem>, val pagination: Pagination?)
}
