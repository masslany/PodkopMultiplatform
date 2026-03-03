package pl.masslany.podkop.features.entries

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.common.composer.ComposerPickedImage
import pl.masslany.podkop.common.composer.ComposerState
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachBottomSheetScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogScreen
import pl.masslany.podkop.common.composer.isComposerImagePickerAvailable
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
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.topbar.TopBarActions

@OptIn(ExperimentalUuidApi::class)
class EntriesViewModel(
    private val authRepository: AuthRepository,
    private val entriesRepository: EntriesRepository,
    private val mediaRepository: MediaRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val savedStateHandle: SavedStateHandle,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntriesActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var currentEntriesSortType: EntriesSortType = EntriesSortType.Hot
    private var currentHotSortType: HotSortType = HotSortType.TwelveHours
    private val screenInstanceId = Uuid.random().toString()
    private val restoredComposerDraft = restoreComposerDraft()

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
        paginator.state,
    ) { state, entries, paginator ->
        state.copy(
            entries = entries,
            isPaginating = paginator is PaginatorState.Loading,
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
        }
    }

    override fun onTopBarAddEntryClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                return@launch
            }

            state.value.composer.photoKey?.let { previousPhotoKey ->
                deletePhoto(photoKey = previousPhotoKey)
            }

            updateState { previousState ->
                previousState.updateComposer {
                    ComposerState.initial.copy(isVisible = true)
                }
            }
        }
    }

    override fun onComposerTextChanged(content: TextFieldValue) {
        updateState { previousState ->
            previousState.updateComposer { composer ->
                composer.copy(content = content)
            }
        }
    }

    override fun onComposerAdultChanged(adult: Boolean) {
        updateState { previousState ->
            previousState.updateComposer { composer ->
                composer.copy(adult = adult)
            }
        }
    }

    override fun onComposerPhotoAttachClicked() {
        viewModelScope.launch {
            val attachResultKey = Uuid.random().toString()
            val attachResult = appNavigator.awaitResult<ComposerMediaAttachResult>(
                target = ComposerMediaAttachBottomSheetScreen(
                    resultKey = attachResultKey,
                    showLocalPicker = isComposerImagePickerAvailable(),
                ),
                key = attachResultKey,
            )

            when (attachResult) {
                ComposerMediaAttachResult.Url -> {
                    val urlResultKey = Uuid.random().toString()
                    val result = appNavigator.awaitResult<ComposerMediaUrlDialogResult>(
                        target = ComposerMediaUrlDialogScreen(resultKey = urlResultKey),
                        key = urlResultKey,
                    )
                    val url = result.url?.trim().orEmpty()
                    if (url.isNotBlank()) {
                        attachComposerPhotoFromUrl(url)
                    }
                }

                ComposerMediaAttachResult.Local -> {
                    val localResultKey = Uuid.random().toString()
                    val result = appNavigator.awaitResult<ComposerMediaPickLocalResult>(
                        target = ComposerMediaPickLocalScreen(resultKey = localResultKey),
                        key = localResultKey,
                    )
                    result.image?.let(::attachComposerPhotoFromLocal)
                }

                ComposerMediaAttachResult.Dismissed -> Unit
            }
        }
    }

    private fun attachComposerPhotoFromUrl(url: String) {
        val normalizedUrl = url.trim()
        if (!isHttpUrl(normalizedUrl)) {
            snackbarManager.tryEmitGenericError()
            return
        }

        uploadComposerPhoto {
            mediaRepository.uploadPhotoFromUrl(
                url = normalizedUrl,
                type = MediaPhotoType.Comments,
            )
        }
    }

    private fun attachComposerPhotoFromLocal(image: ComposerPickedImage) {
        uploadComposerPhoto {
            mediaRepository.uploadPhotoFromDevice(
                bytes = image.bytes,
                fileName = image.fileName,
                mimeType = image.mimeType,
                type = MediaPhotoType.Comments,
            )
        }
    }

    override fun onComposerPhotoRemoved() {
        val currentState = state.value
        if (!currentState.composer.isVisible || currentState.composer.isSubmitting ||
            currentState.composer.isMediaUploading
        ) {
            return
        }

        val photoKey = currentState.composer.photoKey
        if (photoKey == null && currentState.composer.photoUrl == null) {
            return
        }

        updateState { previousState ->
            previousState.updateComposer { composer ->
                composer.copy(
                    photoKey = null,
                    photoUrl = null,
                )
            }
        }

        photoKey?.let { deletePhoto(photoKey = it, showError = true) }
    }

    override fun onComposerDismissed() {
        val photoKey = state.value.composer.photoKey
        updateState(::clearComposerState)
        photoKey?.let { deletePhoto(photoKey = it, showError = true) }
    }

    override fun onComposerSubmit() {
        val currentState = state.value
        if (!currentState.composer.isVisible || currentState.composer.isSubmitting ||
            currentState.composer.isMediaUploading
        ) {
            return
        }

        val content = currentState.composer.content.text.trim()
        if (content.isBlank()) {
            return
        }

        updateState { previousState ->
            previousState.updateComposer { composer ->
                composer.copy(isSubmitting = true)
            }
        }

        viewModelScope.launch {
            entriesRepository.createEntry(
                content = content,
                adult = currentState.composer.adult,
                photoKey = currentState.composer.photoKey,
            ).onSuccess { createdEntry ->
                updateState(::clearComposerState)
                _entryCreatedNavigation.tryEmit(createdEntry.id)
            }.onFailure {
                logger.error("Failed to create entry from entries composer", it)
                updateState { previousState ->
                    previousState.updateComposer { composer ->
                        composer.copy(isSubmitting = false)
                    }
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

    override fun onCleared() {
        _state.value.composer.photoKey?.let { photoKey ->
            deletePhoto(photoKey = photoKey)
        }
        super.onCleared()
    }

    private fun uploadComposerPhoto(upload: suspend () -> Result<pl.masslany.podkop.business.common.domain.models.common.Photo>) {
        val currentState = state.value
        if (!currentState.composer.isVisible || currentState.composer.isSubmitting ||
            currentState.composer.isMediaUploading
        ) {
            return
        }

        val previousPhotoKey = currentState.composer.photoKey

        updateState { previousState ->
            previousState.updateComposer { composer ->
                composer.copy(isMediaUploading = true)
            }
        }

        viewModelScope.launch {
            upload().onSuccess { photo ->
                updateState { previousState ->
                    previousState.updateComposer { composer ->
                        composer.copy(
                            photoKey = photo.key,
                            photoUrl = photo.url,
                            isMediaUploading = false,
                        )
                    }
                }

                if (previousPhotoKey != null && previousPhotoKey != photo.key) {
                    deletePhoto(photoKey = previousPhotoKey)
                }
            }.onFailure {
                logger.error("Failed to upload composer media", it)
                updateState { previousState ->
                    previousState.updateComposer { composer ->
                        composer.copy(isMediaUploading = false)
                    }
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private fun deletePhoto(photoKey: String, showError: Boolean = false) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                mediaRepository.deletePhoto(key = photoKey).onFailure {
                    logger.error("Failed to delete uploaded composer photo for key=$photoKey", it)
                    if (showError) {
                        snackbarManager.tryEmitGenericError()
                    }
                }
            }
        }
    }

    private fun clearComposerState(
        previousState: EntriesScreenState,
    ): EntriesScreenState = previousState.updateComposer { ComposerState.initial }

    private suspend fun resolveFirstPageParam(): Any? = if (authRepository.isLoggedIn()) {
        null
    } else {
        1
    }

    private inline fun updateState(transform: (EntriesScreenState) -> EntriesScreenState) {
        _state.update { previousState ->
            transform(previousState).also(::persistComposerDraft)
        }
    }

    private fun initialState(): EntriesScreenState {
        val draft = restoredComposerDraft
        return EntriesScreenState.initial.copy(
            screenInstanceId = screenInstanceId,
            composer = ComposerState(
                isVisible = draft?.isVisible ?: false,
                content = draft?.content ?: TextFieldValue(),
                replyTarget = null,
                parentCommentId = null,
                adult = draft?.adult ?: false,
                photoKey = draft?.photoKey,
                photoUrl = draft?.photoUrl,
                isSubmitting = false,
                isMediaUploading = false,
            ),
        )
    }

    private fun restoreComposerDraft(): RestoredComposerDraft? {
        val visible = savedStateHandle.get<Boolean>(STATE_COMPOSER_VISIBLE) ?: false
        val text = savedStateHandle.get<String>(STATE_COMPOSER_CONTENT).orEmpty()
        val selectionStart = savedStateHandle.get<Int>(STATE_COMPOSER_SELECTION_START) ?: text.length
        val selectionEnd = savedStateHandle.get<Int>(STATE_COMPOSER_SELECTION_END) ?: text.length
        val adult = savedStateHandle.get<Boolean>(STATE_COMPOSER_ADULT) ?: false
        val photoKey = savedStateHandle.get<String>(STATE_COMPOSER_PHOTO_KEY)
        val photoUrl = savedStateHandle.get<String>(STATE_COMPOSER_PHOTO_URL)

        val hasPersistedDraft = visible || text.isNotEmpty() || adult || photoKey != null || photoUrl != null
        if (!hasPersistedDraft) {
            return null
        }

        val clampedSelectionStart = selectionStart.coerceIn(0, text.length)
        val clampedSelectionEnd = selectionEnd.coerceIn(0, text.length)

        return RestoredComposerDraft(
            isVisible = visible || text.isNotEmpty() || photoKey != null || photoUrl != null,
            content = TextFieldValue(
                text = text,
                selection = TextRange(clampedSelectionStart, clampedSelectionEnd),
            ),
            adult = adult,
            photoKey = photoKey,
            photoUrl = photoUrl,
        )
    }

    private fun persistComposerDraft(state: EntriesScreenState) {
        val hasPersistedDraft =
            state.composer.isVisible ||
                state.composer.content.text.isNotEmpty() ||
                state.composer.adult ||
                state.composer.photoKey != null ||
                state.composer.photoUrl != null
        if (!hasPersistedDraft) {
            savedStateHandle.remove<Any?>(STATE_COMPOSER_VISIBLE)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_CONTENT)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_START)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_END)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_ADULT)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_PHOTO_KEY)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_PHOTO_URL)
            return
        }

        savedStateHandle[STATE_COMPOSER_VISIBLE] = state.composer.isVisible
        savedStateHandle[STATE_COMPOSER_CONTENT] = state.composer.content.text
        savedStateHandle[STATE_COMPOSER_SELECTION_START] = state.composer.content.selection.start
        savedStateHandle[STATE_COMPOSER_SELECTION_END] = state.composer.content.selection.end
        savedStateHandle[STATE_COMPOSER_ADULT] = state.composer.adult
        savedStateHandle[STATE_COMPOSER_PHOTO_KEY] = state.composer.photoKey
        savedStateHandle[STATE_COMPOSER_PHOTO_URL] = state.composer.photoUrl
    }

    private fun isHttpUrl(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("http://") || normalized.startsWith("https://")
    }

    private data class RestoredComposerDraft(
        val isVisible: Boolean,
        val content: TextFieldValue,
        val adult: Boolean,
        val photoKey: String?,
        val photoUrl: String?,
    )

    private companion object {
        const val STATE_COMPOSER_VISIBLE = "entries_composer_visible"
        const val STATE_COMPOSER_CONTENT = "entries_composer_content"
        const val STATE_COMPOSER_SELECTION_START = "entries_composer_selection_start"
        const val STATE_COMPOSER_SELECTION_END = "entries_composer_selection_end"
        const val STATE_COMPOSER_ADULT = "entries_composer_adult"
        const val STATE_COMPOSER_PHOTO_KEY = "entries_composer_photo_key"
        const val STATE_COMPOSER_PHOTO_URL = "entries_composer_photo_url"
    }
}
