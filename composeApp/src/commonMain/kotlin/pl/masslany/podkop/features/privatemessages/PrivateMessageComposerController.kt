package pl.masslany.podkop.features.privatemessages

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.domain.models.common.Photo
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

internal class PrivateMessageComposerController(
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val keyPrefix: String,
) {
    private val initialSnapshot = PrivateMessageComposerSnapshot.empty
    private val restoredComposerDraft = restoreComposerDraft()
    private var ownedPhotoKey: String? = restoredComposerDraft?.ownedPhotoKey
    private var isDismissDialogVisible = false

    private val _state = MutableStateFlow(initialComposerState(restoredComposerDraft))
    val state: StateFlow<ComposerState> = _state.asStateFlow()

    fun onTextChanged(content: TextFieldValue) {
        updateState { previous ->
            previous.copy(content = content)
        }
    }

    fun onAdultChanged(adult: Boolean) {
        updateState { previous ->
            previous.copy(adult = adult)
        }
    }

    fun onPhotoAttachClicked() {
        scope.launch {
            val attachResultKey = "pm-compose-attach-${kotlin.random.Random.nextInt()}"
            val attachResult = appNavigator.awaitResult<ComposerMediaAttachResult>(
                target = ComposerMediaAttachBottomSheetScreen(
                    resultKey = attachResultKey,
                    showLocalPicker = isComposerImagePickerAvailable(),
                ),
                key = attachResultKey,
            )

            when (attachResult) {
                ComposerMediaAttachResult.Url -> {
                    val urlResultKey = "pm-compose-url-${kotlin.random.Random.nextInt()}"
                    val result = appNavigator.awaitResult<ComposerMediaUrlDialogResult>(
                        target = ComposerMediaUrlDialogScreen(resultKey = urlResultKey),
                        key = urlResultKey,
                    )
                    val url = result.url?.trim().orEmpty()
                    if (url.isNotBlank()) {
                        attachPhotoFromUrl(url)
                    }
                }

                ComposerMediaAttachResult.Local -> {
                    val localResultKey = "pm-compose-local-${kotlin.random.Random.nextInt()}"
                    val result = appNavigator.awaitResult<ComposerMediaPickLocalResult>(
                        target = ComposerMediaPickLocalScreen(resultKey = localResultKey),
                        key = localResultKey,
                    )
                    result.image?.let(::attachPhotoFromLocal)
                }

                ComposerMediaAttachResult.Dismissed -> Unit
            }
        }
    }

    fun onPhotoRemoved() {
        val currentState = state.value
        if (currentState.isSubmitting || currentState.isMediaUploading) {
            return
        }

        val photoKey = currentState.photoKey
        if (photoKey == null && currentState.photoUrl == null) {
            return
        }

        if (photoKey != null && ownedPhotoKey == photoKey) {
            ownedPhotoKey = null
            deletePhoto(photoKey = photoKey, showError = true)
        }

        updateState { previous ->
            previous.copy(
                photoKey = null,
                photoUrl = null,
            )
        }
    }

    fun requestClose(
        onConfirmed: () -> Unit,
    ) {
        if (isDismissDialogVisible) {
            return
        }

        scope.launch {
            val composerState = state.value
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

            clearPersistedComposerDraft()
            cleanupCurrentOwnedPhoto()
            onConfirmed()
        }
    }

    fun shouldInterceptBack(): Boolean {
        if (isDismissDialogVisible) {
            return true
        }

        val composerState = state.value
        if (composerState.isSubmitting) {
            return true
        }

        return hasUnsavedChanges(composerState)
    }

    fun <T> submit(
        submitAction: suspend (PrivateMessageComposerPayload) -> Result<T>,
        onSuccess: (T) -> Unit,
    ) {
        val currentState = state.value
        if (currentState.isSubmitting || currentState.isMediaUploading) {
            return
        }

        val content = currentState.content.text.trim()
        if (content.isBlank() && currentState.photoKey == null) {
            return
        }

        updateState { previous ->
            previous.copy(isSubmitting = true)
        }

        scope.launch {
            submitAction(
                PrivateMessageComposerPayload(
                    content = content,
                    adult = currentState.adult,
                    photoKey = currentState.photoKey,
                ),
            ).onSuccess { result ->
                ownedPhotoKey = null
                clearPersistedComposerDraft()
                _state.value = ComposerState.initial
                onSuccess(result)
            }.onFailure {
                logger.error("Failed to submit private message composer", it)
                updateState { previous ->
                    previous.copy(isSubmitting = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    fun onCleared() {
        cleanupCurrentOwnedPhoto()
    }

    private fun attachPhotoFromUrl(url: String) {
        val normalizedUrl = url.trim()
        if (!isHttpUrl(normalizedUrl)) {
            snackbarManager.tryEmitGenericError()
            return
        }

        uploadPhoto {
            mediaRepository.uploadPhotoFromUrl(
                url = normalizedUrl,
                type = MediaPhotoType.Comments,
            )
        }
    }

    private fun attachPhotoFromLocal(image: ComposerPickedImage) {
        uploadPhoto {
            mediaRepository.uploadPhotoFromDevice(
                bytes = image.bytes,
                fileName = image.fileName,
                mimeType = image.mimeType,
                type = MediaPhotoType.Comments,
            )
        }
    }

    private fun uploadPhoto(upload: suspend () -> Result<Photo>) {
        val currentState = state.value
        if (currentState.isSubmitting || currentState.isMediaUploading) {
            return
        }

        val previousOwnedPhotoKey = ownedPhotoKey
        updateState { previous ->
            previous.copy(isMediaUploading = true)
        }

        scope.launch {
            upload().onSuccess { photo ->
                ownedPhotoKey = photo.key
                updateState { previous ->
                    previous.copy(
                        photoKey = photo.key,
                        photoUrl = photo.url,
                        isMediaUploading = false,
                    )
                }

                if (previousOwnedPhotoKey != null && previousOwnedPhotoKey != photo.key) {
                    deletePhoto(photoKey = previousOwnedPhotoKey)
                }
            }.onFailure {
                logger.error("Failed to upload private message media", it)
                updateState { previous ->
                    previous.copy(isMediaUploading = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private fun cleanupCurrentOwnedPhoto() {
        val currentPhotoKey = state.value.photoKey ?: return
        if (ownedPhotoKey != currentPhotoKey) {
            return
        }
        ownedPhotoKey = null
        deletePhoto(photoKey = currentPhotoKey)
    }

    private fun deletePhoto(photoKey: String, showError: Boolean = false) {
        scope.launch {
            withContext(NonCancellable) {
                mediaRepository.deletePhoto(key = photoKey).onFailure {
                    logger.error("Failed to delete private message photo for key=$photoKey", it)
                    if (showError) {
                        snackbarManager.tryEmitGenericError()
                    }
                }
            }
        }
    }

    private fun hasUnsavedChanges(composerState: ComposerState): Boolean =
        composerState.toPrivateMessageComposerSnapshot() != initialSnapshot

    private inline fun updateState(transform: (ComposerState) -> ComposerState) {
        _state.update { previous ->
            transform(previous).also(::persistComposerDraft)
        }
    }

    private fun initialComposerState(restoredDraft: RestoredComposerDraft?): ComposerState {
        if (restoredDraft == null) {
            return ComposerState.initial
        }

        return ComposerState(
            content = restoredDraft.content,
            replyTarget = null,
            parentCommentId = null,
            adult = restoredDraft.adult,
            photoKey = restoredDraft.photoKey,
            photoUrl = restoredDraft.photoUrl,
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

internal data class PrivateMessageComposerPayload(val content: String, val adult: Boolean, val photoKey: String?)

private data class PrivateMessageComposerSnapshot(
    val content: String,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
) {
    companion object {
        val empty = PrivateMessageComposerSnapshot(
            content = "",
            adult = false,
            photoKey = null,
            photoUrl = null,
        )
    }
}

private fun ComposerState.toPrivateMessageComposerSnapshot(): PrivateMessageComposerSnapshot =
    PrivateMessageComposerSnapshot(
        content = content.text,
        adult = adult,
        photoKey = photoKey,
        photoUrl = photoUrl,
    )
