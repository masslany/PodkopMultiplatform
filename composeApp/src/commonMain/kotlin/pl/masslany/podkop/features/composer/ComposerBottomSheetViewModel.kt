package pl.masslany.podkop.features.composer

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
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
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_discard
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.dialog_title_discard_composer_changes

class ComposerBottomSheetViewModel(
    private val screen: ComposerBottomSheetScreen,
    private val entriesRepository: EntriesRepository,
    private val linksRepository: LinksRepository,
    private val mediaRepository: MediaRepository,
    private val appNavigator: AppNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ComposerBottomSheetActions {

    private val initialSnapshot = screen.request.toSnapshot()
    private val keyPrefix = "composer_bottom_sheet_${screen.resultKey}_"
    private val restoredComposerDraft = restoreComposerDraft()

    private var ownedPhotoKey: String? = null
    private var isSubmitted = false
    private var isFinished = false
    private var isDismissDialogVisible = false

    private val _state = MutableStateFlow(
        ComposerBottomSheetState(
            composer = initialComposerState(screen.request, restoredComposerDraft),
        ),
    )
    val state = _state.asStateFlow()

    init {
        ownedPhotoKey = restoredComposerDraft?.ownedPhotoKey
        appNavigator.registerBackHandler(screen) {
            onBackPressed()
        }
    }

    override fun onComposerTextChanged(content: TextFieldValue) {
        updateState { previousState ->
            previousState.copy(
                composer = previousState.composer.copy(content = content),
            )
        }
    }

    override fun onComposerAdultChanged(adult: Boolean) {
        updateState { previousState ->
            previousState.copy(
                composer = previousState.composer.copy(adult = adult),
            )
        }
    }

    override fun onComposerPhotoAttachClicked() {
        viewModelScope.launch {
            val attachResultKey = "composer-attach-${kotlin.random.Random.nextInt()}"
            val attachResult = appNavigator.awaitResult<ComposerMediaAttachResult>(
                target = ComposerMediaAttachBottomSheetScreen(
                    resultKey = attachResultKey,
                    showLocalPicker = isComposerImagePickerAvailable(),
                ),
                key = attachResultKey,
            )

            when (attachResult) {
                ComposerMediaAttachResult.Url -> {
                    val urlResultKey = "composer-url-${kotlin.random.Random.nextInt()}"
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
                    val localResultKey = "composer-local-${kotlin.random.Random.nextInt()}"
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

    override fun onComposerPhotoRemoved() {
        val currentState = state.value
        if (currentState.composer.isSubmitting || currentState.composer.isMediaUploading) {
            return
        }

        val photoKey = currentState.composer.photoKey
        if (photoKey == null && currentState.composer.photoUrl == null) {
            return
        }

        if (photoKey != null && ownedPhotoKey == photoKey) {
            ownedPhotoKey = null
            deletePhoto(photoKey = photoKey, showError = true)
        }

        updateState { previousState ->
            previousState.copy(
                composer = previousState.composer.copy(
                    photoKey = null,
                    photoUrl = null,
                ),
            )
        }
    }

    override fun onComposerDismissed() {
        requestDismiss()
    }

    override fun onComposerSubmit() {
        val currentState = state.value
        if (currentState.composer.isSubmitting || currentState.composer.isMediaUploading) {
            return
        }

        val content = currentState.composer.content.text.trim()
        if (content.isBlank()) {
            return
        }

        updateState { previousState ->
            previousState.copy(
                composer = previousState.composer.copy(isSubmitting = true),
            )
        }

        viewModelScope.launch {
            submitComposer(
                request = screen.request,
                content = content,
                adult = currentState.composer.adult,
                photoKey = currentState.composer.photoKey,
            ).onSuccess { resource ->
                if (isFinished) {
                    return@onSuccess
                }
                isSubmitted = true
                isFinished = true
                clearPersistedComposerDraft()
                appNavigator.unregisterBackHandler(screen)
                appNavigator.sendResult(
                    key = screen.resultKey,
                    result = ComposerResult.Submitted(resource = resource),
                )
            }.onFailure {
                logger.error("Failed to submit standalone composer", it)
                updateState { previousState ->
                    previousState.copy(
                        composer = previousState.composer.copy(isSubmitting = false),
                    )
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onCleared() {
        appNavigator.unregisterBackHandler(screen)
        if (!isSubmitted) {
            cleanupCurrentOwnedPhoto()
        }
        super.onCleared()
    }

    private fun onBackPressed(): Boolean {
        requestDismiss()
        return true
    }

    private fun requestDismiss() {
        if (isFinished || isDismissDialogVisible) {
            return
        }

        viewModelScope.launch {
            val composerState = state.value.composer
            if (composerState.isSubmitting) {
                return@launch
            }

            if (hasUnsavedChanges(composerState)) {
                isDismissDialogVisible = true
                val dialog = GenericDialog.fromResources(
                    title = Res.string.dialog_title_discard_composer_changes,
                    positiveText = Res.string.dialog_button_discard,
                    negativeText = Res.string.dialog_button_dismiss,
                )
                val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
                isDismissDialogVisible = false

                if (!confirmed) {
                    return@launch
                }
            }

            completeDismiss()
        }
    }

    private fun completeDismiss() {
        if (isFinished) {
            return
        }

        isFinished = true
        clearPersistedComposerDraft()
        cleanupCurrentOwnedPhoto()
        appNavigator.unregisterBackHandler(screen)
        appNavigator.sendResult(
            key = screen.resultKey,
            result = ComposerResult.Dismissed,
        )
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

    private fun uploadComposerPhoto(upload: suspend () -> Result<Photo>) {
        val currentState = state.value
        if (currentState.composer.isSubmitting || currentState.composer.isMediaUploading) {
            return
        }

        val previousOwnedPhotoKey = ownedPhotoKey

        updateState { previousState ->
            previousState.copy(
                composer = previousState.composer.copy(isMediaUploading = true),
            )
        }

        viewModelScope.launch {
            upload().onSuccess { photo ->
                ownedPhotoKey = photo.key
                updateState { previousState ->
                    previousState.copy(
                        composer = previousState.composer.copy(
                            photoKey = photo.key,
                            photoUrl = photo.url,
                            isMediaUploading = false,
                        ),
                    )
                }

                if (previousOwnedPhotoKey != null && previousOwnedPhotoKey != photo.key) {
                    deletePhoto(photoKey = previousOwnedPhotoKey)
                }
            }.onFailure {
                logger.error("Failed to upload standalone composer media", it)
                updateState { previousState ->
                    previousState.copy(
                        composer = previousState.composer.copy(isMediaUploading = false),
                    )
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private suspend fun submitComposer(
        request: ComposerRequest,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> = when (request) {
        is ComposerRequest.CreateEntry -> {
            entriesRepository.createEntry(
                content = content,
                adult = adult,
                photoKey = photoKey,
            )
        }

        is ComposerRequest.CreateEntryComment -> {
            entriesRepository.createEntryComment(
                entryId = request.entryId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            )
        }

        is ComposerRequest.CreateLinkComment -> {
            val parentCommentId = request.parentCommentId
            if (parentCommentId == null) {
                linksRepository.createLinkComment(
                    linkId = request.linkId,
                    content = content,
                    adult = adult,
                    photoKey = photoKey,
                )
            } else {
                linksRepository.createLinkCommentReply(
                    linkId = request.linkId,
                    commentId = parentCommentId,
                    content = content,
                    adult = adult,
                    photoKey = photoKey,
                )
            }
        }

        is ComposerRequest.EditEntry -> {
            entriesRepository.updateEntry(
                entryId = request.entryId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            )
        }

        is ComposerRequest.EditEntryComment -> {
            entriesRepository.updateEntryComment(
                entryId = request.entryId,
                commentId = request.commentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            )
        }

        is ComposerRequest.EditLinkComment -> {
            linksRepository.updateLinkComment(
                linkId = request.linkId,
                commentId = request.commentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            )
        }
    }

    private fun cleanupCurrentOwnedPhoto() {
        val currentPhotoKey = state.value.composer.photoKey ?: return
        if (ownedPhotoKey != currentPhotoKey) {
            return
        }
        ownedPhotoKey = null
        deletePhoto(photoKey = currentPhotoKey)
    }

    private fun deletePhoto(photoKey: String, showError: Boolean = false) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                mediaRepository.deletePhoto(key = photoKey).onFailure {
                    logger.error("Failed to delete standalone composer photo for key=$photoKey", it)
                    if (showError) {
                        snackbarManager.tryEmitGenericError()
                    }
                }
            }
        }
    }

    private fun hasUnsavedChanges(composerState: ComposerState): Boolean = composerState.toSnapshot() != initialSnapshot

    private inline fun updateState(transform: (ComposerBottomSheetState) -> ComposerBottomSheetState) {
        _state.update { previousState ->
            transform(previousState).also { newState ->
                persistComposerDraft(newState.composer)
            }
        }
    }

    private fun initialComposerState(
        request: ComposerRequest,
        restoredDraft: RestoredComposerDraft?,
    ): ComposerState {
        if (restoredDraft != null) {
            return ComposerState(
                content = restoredDraft.content,
                replyTarget = request.prefill.replyTarget,
                parentCommentId = (request as? ComposerRequest.CreateLinkComment)?.parentCommentId,
                adult = restoredDraft.adult,
                photoKey = restoredDraft.photoKey,
                photoUrl = restoredDraft.photoUrl,
                isSubmitting = false,
                isMediaUploading = false,
            )
        }

        val prefill = request.prefill
        val text = prefill.content

        return ComposerState(
            content = TextFieldValue(
                text = text,
                selection = TextRange(text.length),
            ),
            replyTarget = prefill.replyTarget,
            parentCommentId = (request as? ComposerRequest.CreateLinkComment)?.parentCommentId,
            adult = prefill.adult,
            photoKey = prefill.photoKey,
            photoUrl = prefill.photoUrl,
            isSubmitting = false,
            isMediaUploading = false,
        )
    }

    private fun restoreComposerDraft(): RestoredComposerDraft? {
        val hasDraft = savedStateHandle.get<Boolean>(key(STATE_HAS_DRAFT)) == true
        if (!hasDraft) {
            return null
        }

        val text = savedStateHandle.get<String>(key(STATE_CONTENT)).orEmpty()
        val selectionStart = savedStateHandle.get<Int>(key(STATE_SELECTION_START)) ?: text.length
        val selectionEnd = savedStateHandle.get<Int>(key(STATE_SELECTION_END)) ?: text.length
        val clampedSelectionStart = selectionStart.coerceIn(0, text.length)
        val clampedSelectionEnd = selectionEnd.coerceIn(0, text.length)

        return RestoredComposerDraft(
            content = TextFieldValue(
                text = text,
                selection = TextRange(clampedSelectionStart, clampedSelectionEnd),
            ),
            adult = savedStateHandle.get<Boolean>(key(STATE_ADULT)) ?: false,
            photoKey = savedStateHandle.get<String>(key(STATE_PHOTO_KEY)),
            photoUrl = savedStateHandle.get<String>(key(STATE_PHOTO_URL)),
            ownedPhotoKey = savedStateHandle.get<String>(key(STATE_OWNED_PHOTO_KEY)),
        )
    }

    private fun persistComposerDraft(composer: ComposerState) {
        savedStateHandle[key(STATE_HAS_DRAFT)] = true
        savedStateHandle[key(STATE_CONTENT)] = composer.content.text
        savedStateHandle[key(STATE_SELECTION_START)] = composer.content.selection.start
        savedStateHandle[key(STATE_SELECTION_END)] = composer.content.selection.end
        savedStateHandle[key(STATE_ADULT)] = composer.adult
        savedStateHandle[key(STATE_PHOTO_KEY)] = composer.photoKey
        savedStateHandle[key(STATE_PHOTO_URL)] = composer.photoUrl
        savedStateHandle[key(STATE_OWNED_PHOTO_KEY)] = ownedPhotoKey
    }

    private fun clearPersistedComposerDraft() {
        savedStateHandle.remove<Any?>(key(STATE_HAS_DRAFT))
        savedStateHandle.remove<Any?>(key(STATE_CONTENT))
        savedStateHandle.remove<Any?>(key(STATE_SELECTION_START))
        savedStateHandle.remove<Any?>(key(STATE_SELECTION_END))
        savedStateHandle.remove<Any?>(key(STATE_ADULT))
        savedStateHandle.remove<Any?>(key(STATE_PHOTO_KEY))
        savedStateHandle.remove<Any?>(key(STATE_PHOTO_URL))
        savedStateHandle.remove<Any?>(key(STATE_OWNED_PHOTO_KEY))
    }

    private fun key(suffix: String): String = "$keyPrefix$suffix"

    private fun isHttpUrl(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("http://") || normalized.startsWith("https://")
    }

    private data class RestoredComposerDraft(
        val content: TextFieldValue,
        val adult: Boolean,
        val photoKey: String?,
        val photoUrl: String?,
        val ownedPhotoKey: String?,
    )

    private companion object {
        const val STATE_HAS_DRAFT = "has_draft"
        const val STATE_CONTENT = "content"
        const val STATE_SELECTION_START = "selection_start"
        const val STATE_SELECTION_END = "selection_end"
        const val STATE_ADULT = "adult"
        const val STATE_PHOTO_KEY = "photo_key"
        const val STATE_PHOTO_URL = "photo_url"
        const val STATE_OWNED_PHOTO_KEY = "owned_photo_key"
    }
}

private data class ComposerSnapshot(
    val content: String,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
)

private fun ComposerState.toSnapshot(): ComposerSnapshot = ComposerSnapshot(
    content = content.text,
    adult = adult,
    photoKey = photoKey,
    photoUrl = photoUrl,
)

private fun ComposerRequest.toSnapshot(): ComposerSnapshot = ComposerSnapshot(
    content = prefill.content,
    adult = prefill.adult,
    photoKey = prefill.photoKey,
    photoUrl = prefill.photoUrl,
)
