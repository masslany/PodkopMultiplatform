package pl.masslany.podkop.features.entrydetails

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
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
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState
import pl.masslany.podkop.features.topbar.TopBarActions

class EntryDetailsViewModel(
    private val screen: EntryDetailsScreen,
    private val entriesRepository: EntriesRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val mediaRepository: MediaRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val savedStateHandle: SavedStateHandle,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntryDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private val entryId = screen.id
    private val restoredComposerDraft = restoreComposerDraft()
    private var isPendingComposerIntentConsumed = screen.pendingComposerIntent == null || restoredComposerDraft != null

    private var entryResource: ResourceItem? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated entry comments for id=$entryId", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        entriesRepository.getEntryComments(
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            entryId = entryId,
        )
    }

    private val _state = MutableStateFlow(initialState())
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, comments, paginator ->
        logger.debug("Entry details comments updated: $comments")
        val holderEntry = comments
            .filterIsInstance<EntryItemState>()
            .firstOrNull { entry -> entry.id == entryId }
        val holderComments = comments
            .filterNot { item -> item is EntryItemState && item.id == entryId }
            .toImmutableList()
        state.copy(
            entry = holderEntry ?: state.entry,
            comments = holderComments,
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), initialState())

    init {
        resourceItemStateHolder.init(viewModelScope)
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
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
            val attachResultKey = "entry-details-attach-$entryId-${kotlin.random.Random.nextInt()}"
            val attachResult = appNavigator.awaitResult<ComposerMediaAttachResult>(
                target = ComposerMediaAttachBottomSheetScreen(
                    resultKey = attachResultKey,
                    showLocalPicker = isComposerImagePickerAvailable(),
                ),
                key = attachResultKey,
            )

            when (attachResult) {
                ComposerMediaAttachResult.Url -> {
                    val urlResultKey = "entry-details-url-$entryId-${kotlin.random.Random.nextInt()}"
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
                    val localResultKey = "entry-details-local-$entryId-${kotlin.random.Random.nextInt()}"
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
        val currentState = _state.value
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
        val photoKey = _state.value.composer.photoKey
        updateState(::clearComposerState)
        photoKey?.let { deletePhoto(photoKey = it, showError = true) }
    }

    override fun onComposerSubmit() {
        val currentState = _state.value
        if (!currentState.isLoggedIn ||
            !currentState.composer.isVisible ||
            currentState.composer.isSubmitting ||
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
            entriesRepository.createEntryComment(
                entryId = entryId,
                content = content,
                adult = currentState.composer.adult,
                photoKey = currentState.composer.photoKey,
            ).onSuccess { createdResource ->
                resourceItemStateHolder.appendData(listOf(createdResource))
                updateState(::clearComposerState)
            }.onFailure {
                logger.error("Failed to submit entry reply composer for entryId=$entryId", it)
                updateState { previousState ->
                    previousState.updateComposer { composer ->
                        composer.copy(isSubmitting = false)
                    }
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        if (entryId != this.entryId) return
        showComposerForAuthor(author = author)
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        if (entryId != this.entryId) return
        showComposerForAuthor(author = author)
    }

    private fun loadContent(isRefreshing: Boolean) {
        _state.update { previousState ->
            previousState
                .updateLoading(!isRefreshing)
                .updateError(false)
                .updateCommentsError(false)
                .updateRefreshing(isRefreshing)
        }

        viewModelScope.launch {
            coroutineScope {
                val viewerContextDeferred = async {
                    resolveViewerContext()
                }
                val entryDeferred = async {
                    entriesRepository.getEntry(entryId = entryId)
                }
                val commentsDeferred = async {
                    entriesRepository.getEntryComments(
                        entryId = entryId,
                        page = 1,
                    )
                }

                val isEntryLoaded = entryDeferred.await()
                    .onSuccess { entry ->
                        entryResource = entry
                        resourceItemStateHolder.updateData(listOf(entry))
                        updateState { previousState ->
                            previousState.copy(entry = entry.toResourceItemState())
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry details for id=$entryId", it)
                    }
                    .isSuccess

                applyViewerContext(viewerContextDeferred.await())

                commentsDeferred.await()
                    .onSuccess { comments ->
                        resourceItemStateHolder.updateData(topLevelEntryAndComments(comments.data))
                        paginator.setup(comments.pagination, comments.data.size)
                        updateState { previousState ->
                            previousState.updateCommentsError(false)
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry comments for id=$entryId", it)
                        resourceItemStateHolder.updateData(topLevelEntryAndComments(emptyList()))
                        updateState { previousState ->
                            previousState.updateCommentsError(true)
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                updateState { previousState ->
                    previousState.updateError(!isEntryLoaded)
                }
            }

            updateState { previousState ->
                previousState
                    .updateLoading(false)
                    .updateRefreshing(false)
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

    private fun uploadComposerPhoto(upload: suspend () -> Result<Photo>) {
        val currentState = _state.value
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
                logger.error("Failed to upload entry details composer media for entryId=$entryId", it)
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
                    logger.error("Failed to delete uploaded entry details composer photo for key=$photoKey", it)
                    if (showError) {
                        snackbarManager.tryEmitGenericError()
                    }
                }
            }
        }
    }

    private fun showComposerForAuthor(author: String?, canShowComposer: Boolean = _state.value.isLoggedIn) {
        if (!canShowComposer) {
            return
        }

        _state.value.composer.photoKey?.let { previousPhotoKey ->
            deletePhoto(photoKey = previousPhotoKey)
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefill = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        updateState { previousState ->
            previousState.updateComposer {
                ComposerState(
                    isVisible = true,
                    replyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                    content = TextFieldValue(text = prefill, selection = TextRange(prefill.length)),
                    parentCommentId = null,
                    adult = false,
                    photoKey = null,
                    photoUrl = null,
                    isSubmitting = false,
                    isMediaUploading = false,
                )
            }
        }
    }

    private fun applyViewerContext(viewerContext: ViewerContext) {
        updateState { previousState ->
            if (!viewerContext.isLoggedIn) {
                clearComposerState(
                    previousState.copy(
                        isLoggedIn = false,
                        currentUsername = null,
                    ),
                )
            } else {
                previousState.copy(
                    isLoggedIn = true,
                    currentUsername = viewerContext.username,
                )
            }
        }
        maybeApplyPendingComposerIntent(canShowComposer = viewerContext.isLoggedIn)
    }

    private fun maybeApplyPendingComposerIntent(canShowComposer: Boolean) {
        if (isPendingComposerIntentConsumed) {
            return
        }
        isPendingComposerIntentConsumed = true

        if (!canShowComposer) {
            return
        }

        when (screen.pendingComposerIntent?.type) {
            EntryComposerIntentType.Reply -> showComposerForAuthor(
                author = screen.pendingComposerIntent.author,
                canShowComposer = canShowComposer,
            )

            null -> Unit
        }
    }

    private fun clearComposerState(
        previousState: EntryDetailsScreenState,
    ): EntryDetailsScreenState = previousState.updateComposer { ComposerState.initial }

    private suspend fun resolveViewerContext(): ViewerContext {
        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) {
            return ViewerContext(
                isLoggedIn = false,
                username = null,
            )
        }

        val username = profileRepository.getProfileShort()
            .onFailure {
                logger.error("Failed to resolve current profile short", it)
            }
            .getOrNull()
            ?.name

        return ViewerContext(
            isLoggedIn = true,
            username = username,
        )
    }

    private fun topLevelEntryAndComments(
        comments: List<ResourceItem>,
    ): List<ResourceItem> {
        val topLevel = entryResource
        return if (topLevel == null) {
            comments
        } else {
            buildList(comments.size + 1) {
                add(topLevel)
                addAll(comments)
            }
        }
    }

    private inline fun updateState(transform: (EntryDetailsScreenState) -> EntryDetailsScreenState) {
        _state.update { previousState ->
            transform(previousState).also(::persistComposerDraft)
        }
    }

    private fun initialState(): EntryDetailsScreenState {
        val draft = restoredComposerDraft
        return EntryDetailsScreenState.initial.copy(
            isLoading = true,
            composer = ComposerState(
                isVisible = draft?.isVisible ?: false,
                content = draft?.content ?: TextFieldValue(),
                replyTarget = draft?.replyTarget,
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
        val replyTarget = savedStateHandle.get<String>(STATE_COMPOSER_REPLY_TARGET)
        val adult = savedStateHandle.get<Boolean>(STATE_COMPOSER_ADULT) ?: false
        val photoKey = savedStateHandle.get<String>(STATE_COMPOSER_PHOTO_KEY)
        val photoUrl = savedStateHandle.get<String>(STATE_COMPOSER_PHOTO_URL)

        val hasPersistedDraft = visible ||
            text.isNotEmpty() ||
            replyTarget != null ||
            adult ||
            photoKey != null ||
            photoUrl != null
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
            replyTarget = replyTarget,
            adult = adult,
            photoKey = photoKey,
            photoUrl = photoUrl,
        )
    }

    private fun persistComposerDraft(state: EntryDetailsScreenState) {
        val hasPersistedDraft = state.composer.isVisible ||
            state.composer.content.text.isNotEmpty() ||
            state.composer.replyTarget != null ||
            state.composer.adult ||
            state.composer.photoKey != null ||
            state.composer.photoUrl != null

        if (!hasPersistedDraft) {
            savedStateHandle.remove<Any?>(STATE_COMPOSER_VISIBLE)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_CONTENT)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_START)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_END)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_REPLY_TARGET)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_ADULT)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_PHOTO_KEY)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_PHOTO_URL)
            return
        }

        savedStateHandle[STATE_COMPOSER_VISIBLE] = state.composer.isVisible
        savedStateHandle[STATE_COMPOSER_CONTENT] = state.composer.content.text
        savedStateHandle[STATE_COMPOSER_SELECTION_START] = state.composer.content.selection.start
        savedStateHandle[STATE_COMPOSER_SELECTION_END] = state.composer.content.selection.end
        savedStateHandle[STATE_COMPOSER_REPLY_TARGET] = state.composer.replyTarget
        savedStateHandle[STATE_COMPOSER_ADULT] = state.composer.adult
        savedStateHandle[STATE_COMPOSER_PHOTO_KEY] = state.composer.photoKey
        savedStateHandle[STATE_COMPOSER_PHOTO_URL] = state.composer.photoUrl
    }

    private fun isHttpUrl(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("http://") || normalized.startsWith("https://")
    }

    private data class ViewerContext(val isLoggedIn: Boolean, val username: String?)

    private data class RestoredComposerDraft(
        val isVisible: Boolean,
        val content: TextFieldValue,
        val replyTarget: String?,
        val adult: Boolean,
        val photoKey: String?,
        val photoUrl: String?,
    )

    private companion object {
        const val STATE_COMPOSER_VISIBLE = "entry_details_composer_visible"
        const val STATE_COMPOSER_CONTENT = "entry_details_composer_content"
        const val STATE_COMPOSER_SELECTION_START = "entry_details_composer_selection_start"
        const val STATE_COMPOSER_SELECTION_END = "entry_details_composer_selection_end"
        const val STATE_COMPOSER_REPLY_TARGET = "entry_details_composer_reply_target"
        const val STATE_COMPOSER_ADULT = "entry_details_composer_adult"
        const val STATE_COMPOSER_PHOTO_KEY = "entry_details_composer_photo_key"
        const val STATE_COMPOSER_PHOTO_URL = "entry_details_composer_photo_url"
    }
}
