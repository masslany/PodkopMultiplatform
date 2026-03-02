package pl.masslany.podkop.features.tag

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
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class TagViewModel(
    private val tag: String,
    private val authRepository: AuthRepository,
    private val tagsRepository: TagsRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    TagActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentSort: TagsSort = TagsSort.All
    private var currentType: TagsType = TagsType.All
    private val screenInstanceId = Uuid.random().toString()

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated tag stream for $tag", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        tagsRepository.getTagStream(
            tagName = tag,
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            limit = null,
            sort = currentSort,
            type = currentType,
        )
    }

    private val _state = MutableStateFlow(
        TagScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, resources, paginator ->
        state.copy(
            resources = resources,
            galleryItems = resources.toTagGalleryItems(),
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        TagScreenState.initial.copy(screenInstanceId = screenInstanceId),
    )

    init {
        resourceItemStateHolder.init(viewModelScope)

        viewModelScope.launch {
            _state.update { previousState ->
                previousState.copy(
                    tag = tag,
                    isLoggedIn = authRepository.isLoggedIn(),
                    sortMenuState = DropdownMenuState(
                        items = tagsRepository.getTagsSorts()
                            .map { it.toDropdownMenuItemType() }
                            .toImmutableList(),
                        selected = currentSort.toDropdownMenuItemType(),
                        expanded = false,
                    ),
                    typeMenuState = DropdownMenuState(
                        items = tagsRepository.getTagsTypes()
                            .map { it.toDropdownMenuItemType() }
                            .toImmutableList(),
                        selected = currentType.toDropdownMenuItemType(),
                        expanded = false,
                    ),
                )
            }
            loadTagDetails()
            loadTagStream()
        }
    }

    override fun onSortSelected(sortType: DropdownMenuItemType) {
        currentSort = sortType.toTagsSort()
        _state.update { previousState ->
            previousState
                .updateSortMenuSelected(sortType)
                .updateError(false)
                .updateRefreshing(true)
        }
        loadTagStream()
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

    override fun onTypeSelected(type: DropdownMenuItemType) {
        currentType = type.toTagsType()
        _state.update { previousState ->
            previousState
                .updateTypeMenuSelected(type)
                .updateError(false)
                .updateRefreshing(true)
        }
        loadTagStream()
    }

    override fun onTypeExpandedChanged(expanded: Boolean) {
        _state.update { previousState ->
            previousState.updateTypeMenuExpanded(expanded)
        }
    }

    override fun onTypeDismissed() {
        _state.update { previousState ->
            previousState.updateTypeMenuExpanded(false)
        }
    }

    override fun onGalleryModeToggled() {
        _state.update { previousState ->
            previousState.updateGalleryMode(!previousState.isGalleryMode)
        }
    }

    override fun onRefresh() {
        _state.update { previousState ->
            previousState
                .updateError(false)
                .updateRefreshing(true)
        }
        loadTagDetails()
        loadTagStream()
    }

    override fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItems: Int,
    ): Boolean = paginator.shouldPaginate(lastVisibleIndex, totalItems)

    override fun paginate() {
        paginator.paginate()
    }

    private fun loadTagDetails() {
        viewModelScope.launch {
            tagsRepository.getTagDetails(tagName = tag)
                .onSuccess { details ->
                    _state.update { previousState ->
                        previousState
                            .updateBannerUrl(
                                details.media?.photo?.url ?: details.media?.embed?.thumbnail.orEmpty(),
                            )
                            .updateTagContentError(false)
                    }
                }
                .onFailure {
                    logger.error("Failed to load tag details for $tag", it)
                    _state.update { previousState ->
                        previousState.updateTagContentError(true)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun loadTagStream() {
        viewModelScope.launch {
            tagsRepository.getTagStream(
                tagName = tag,
                page = resolveFirstPageParam(),
                limit = null,
                sort = currentSort,
                type = currentType,
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
                    logger.error("Failed to load tag stream for $tag", it)
                    val shouldShowErrorScreen = state.value.resources.isEmpty()
                    _state.update { previousState ->
                        previousState
                            .updateLoading(false)
                            .updateError(shouldShowErrorScreen)
                            .updateRefreshing(false)
                    }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private suspend fun resolveFirstPageParam(): Any? = if (authRepository.isLoggedIn()) {
        null
    } else {
        1
    }
}
