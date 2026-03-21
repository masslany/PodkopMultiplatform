package pl.masslany.podkop.features.linksubmission.linkdraft

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.common.composer.ComposerPickedImage
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachBottomSheetScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogResult
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogScreen
import pl.masslany.podkop.common.composer.isComposerImagePickerAvailable
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.linksubmission.AddLinkScreen
import pl.masslany.podkop.features.linksubmission.LinkDraftScreen
import pl.masslany.podkop.features.linksubmission.models.AddLinkSnapshot
import pl.masslany.podkop.features.linksubmission.models.AddLinkSuggestedImageState
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState
import pl.masslany.podkop.features.topbar.TopBarActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_add_link_success

@OptIn(ExperimentalUuidApi::class)
internal class LinkDraftViewModel(
    private val screen: LinkDraftScreen,
    private val linksRepository: LinksRepository,
    private val mediaRepository: MediaRepository,
    private val tagsRepository: TagsRepository,
    private val appNavigator: AppNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val topBarActions: TopBarActions,
) : ViewModel(),
    LinkDraftActions,
    TopBarActions by topBarActions {

    private val restoredDraft = restoreDraft()
        ?.takeIf { it.state.draftKey == screen.draftKey }

    private var ownedPhotoKey: String? = restoredDraft?.ownedPhotoKey
    private var isSubmitted = false
    private var isFinished = false
    private var tagSuggestionsJob: Job? = null
    private var suggestedImageUpdateJob: Job? = null
    private var suggestedImageUpdateSequence = 0L

    private val _state = MutableStateFlow(
        restoredDraft?.state ?: LinkDraftScreenState.initial.copy(
            draftKey = screen.draftKey,
            isLoadingDraft = true,
        ),
    )
    val state = _state.asStateFlow()

    init {
        if (restoredDraft == null) {
            loadDraft(screen.draftKey)
        }
    }

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTitleChanged(value: String) {
        updateState { previous ->
            previous.copy(title = value)
        }
    }

    override fun onDescriptionChanged(value: String) {
        updateState { previous ->
            previous.copy(description = value)
        }
    }

    override fun onTagInputChanged(value: String) {
        updateState { previous ->
            previous.withTagInputChanged(value)
        }
        requestTagSuggestions(tagInput = state.value.tagInput)
    }

    override fun onPendingTagSubmitted() {
        if (normalizeLinkTags(state.value.tagInput).isEmpty()) {
            return
        }

        updateState { previous ->
            previous.withPendingTagSubmitted()
        }
        tagSuggestionsJob?.cancel()
        tagSuggestionsJob = null
    }

    override fun onTagSuggestionClicked(tag: String) {
        if (normalizeLinkTagQuery(tag).isBlank()) {
            return
        }

        updateState { previous ->
            previous.withTagSuggestionSelected(tag)
        }
        tagSuggestionsJob?.cancel()
        tagSuggestionsJob = null
    }

    override fun onTagRemoved(tag: String) {
        updateState { previous ->
            previous.copy(
                tags = previous.tags.filterNot { it == tag }.toImmutableList(),
            )
        }
        requestTagSuggestions(tagInput = state.value.tagInput)
    }

    override fun onAdultChanged(value: Boolean) {
        updateState { previous ->
            previous.copy(adult = value)
        }
    }

    /**
     * Applies the new suggested image selection optimistically and persists it in the background.
     *
     * The sequence guard prevents an older in-flight request from rolling back a newer user choice.
     * Example: the user swipes to image A, then quickly to image B. If request A fails after
     * request B has already started, we only want to revert the UI when that failure still belongs
     * to the latest attempted selection. We keep this explicit guard because cancellation is
     * best-effort and older requests may still complete after a newer selection has been made.
     */
    override fun onSuggestedImageChanged(index: Int) {
        val currentState = state.value
        val draftKey = currentState.draftKey ?: return
        if (currentState.isLoadingDraft ||
            index !in currentState.suggestedImages.indices ||
            currentState.selectedSuggestedImageIndex == index ||
            currentState.isPublishing ||
            currentState.isMediaUploading
        ) {
            return
        }

        val previousIndex = currentState.selectedSuggestedImageIndex
        updateState { previous ->
            previous.copy(selectedSuggestedImageIndex = index)
        }

        val requestSequence = ++suggestedImageUpdateSequence
        suggestedImageUpdateJob?.cancel()
        suggestedImageUpdateJob = viewModelScope.launch {
            linksRepository.updateLinkDraft(
                key = draftKey,
                request = state.value.toUpdateLinkDraftRequest(selectedImageIndex = index),
            ).onFailure {
                logger.error("Failed to update add-link draft image selection key=$draftKey", it)
                if (requestSequence == suggestedImageUpdateSequence &&
                    state.value.selectedSuggestedImageIndex == index
                ) {
                    updateState { previous ->
                        previous.copy(selectedSuggestedImageIndex = previousIndex)
                    }
                    snackbarManager.tryEmitGenericError()
                }
            }
        }
    }

    override fun onPhotoAttachClicked() {
        val currentState = state.value
        if (currentState.isLoadingDraft || currentState.isMediaUploading || currentState.isPublishing) {
            return
        }

        viewModelScope.launch {
            val attachResultKey = "add-link-attach-${Uuid.random()}"
            val attachResult = appNavigator.awaitResult<ComposerMediaAttachResult>(
                target = ComposerMediaAttachBottomSheetScreen(
                    resultKey = attachResultKey,
                    showLocalPicker = isComposerImagePickerAvailable(),
                ),
                key = attachResultKey,
            )

            when (attachResult) {
                ComposerMediaAttachResult.Url -> {
                    val urlResultKey = "add-link-url-${Uuid.random()}"
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
                    val localResultKey = "add-link-local-${Uuid.random()}"
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

    override fun onPhotoRemoved() {
        val currentState = state.value
        if (currentState.isLoadingDraft || currentState.isMediaUploading || currentState.isPublishing) {
            return
        }

        val currentPhotoKey = currentState.photoKey
        if (currentPhotoKey != null && currentPhotoKey == ownedPhotoKey) {
            ownedPhotoKey = null
            deletePhoto(currentPhotoKey, showError = true)
        }

        updateState { previous ->
            previous.copy(
                photoKey = null,
                photoUrl = null,
            )
        }
    }

    override fun onSubmitClicked() {
        val currentState = state.value
        if (!currentState.canSubmit) {
            return
        }

        val draftKey = currentState.draftKey ?: return
        val request = currentState.toValidatedPublishRequest() ?: return
        val normalizedTags = request.tags

        updateState { previous ->
            previous.copy(
                tags = normalizedTags.toImmutableList(),
                tagInput = "",
                isPublishing = true,
            )
        }

        viewModelScope.launch {
            linksRepository.publishLinkDraft(
                key = draftKey,
                request = request,
            ).onSuccess {
                if (isFinished) {
                    return@onSuccess
                }

                ownedPhotoKey = null
                isSubmitted = true
                isFinished = true
                clearPersistedState()
                snackbarManager.tryEmit(
                    SnackbarEvent(
                        message = SnackbarMessage.Resource(Res.string.snackbar_add_link_success),
                    ),
                )
                popAddLinkFlow()
            }.onFailure {
                logger.error("Failed to publish add-link draft key=$draftKey", it)
                updateState { previous ->
                    previous.copy(isPublishing = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onCancelClicked() {
        appNavigator.back()
    }

    override fun onCleared() {
        tagSuggestionsJob?.cancel()
        suggestedImageUpdateJob?.cancel()
        if (!isSubmitted && !isFinished) {
            runBlocking {
                withContext(NonCancellable) {
                    val draftSaved = persistDraftOnClearedIfNeeded(state.value)
                    if (!draftSaved) {
                        cleanupCurrentOwnedPhotoImmediately()
                    }
                }
            }
        }
        super.onCleared()
    }

    private fun loadDraft(key: String) {
        updateState { previous ->
            previous.copy(
                draftKey = key,
                isLoadingDraft = true,
            )
        }

        viewModelScope.launch {
            linksRepository.getLinkDraft(key)
                .onSuccess { draftDetails ->
                    ownedPhotoKey = null
                    updateState { previous ->
                        previous.withLoadedDraft(draftDetails)
                    }
                }
                .onFailure {
                    logger.error("Failed to load add-link draft key=$key", it)
                    updateState { previous ->
                        previous.copy(isLoadingDraft = false)
                    }
                    snackbarManager.tryEmitGenericError()
                    appNavigator.back()
                }
        }
    }

    private fun requestTagSuggestions(tagInput: String) {
        val normalizedQuery = normalizeLinkTagQuery(tagInput)
        tagSuggestionsJob?.cancel()
        tagSuggestionsJob = null

        if (normalizedQuery.length < MIN_TAG_SUGGESTION_QUERY_LENGTH) {
            updateTransientState { previous ->
                previous.copy(
                    tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
                    isLoadingTagSuggestions = false,
                )
            }
            return
        }

        updateTransientState { previous ->
            previous.copy(
                tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
                isLoadingTagSuggestions = true,
            )
        }

        tagSuggestionsJob = viewModelScope.launch {
            delay(TAG_SUGGESTION_DEBOUNCE_MILLIS)
            tagsRepository.getAutoCompleteTags(query = normalizedQuery)
                .onSuccess { autocomplete ->
                    updateTransientState { previous ->
                        if (previous.currentTagAutocompleteQuery != normalizedQuery) {
                            return@updateTransientState previous
                        }

                        previous.copy(
                            tagSuggestions = autocomplete.tags
                                .map {
                                    AddLinkTagSuggestionState(
                                        name = normalizeLinkTagQuery(it.name),
                                        observedQuantity = it.observedQuantity,
                                    )
                                }
                                .filter { suggestion -> suggestion.name.isNotBlank() }
                                .filterNot { suggestion -> previous.tags.contains(suggestion.name) }
                                .distinctBy(AddLinkTagSuggestionState::name)
                                .toImmutableList(),
                            isLoadingTagSuggestions = false,
                        )
                    }
                }
                .onFailure {
                    logger.error("Failed to load add-link tag autocomplete for query=$normalizedQuery", it)
                    updateTransientState { previous ->
                        if (previous.currentTagAutocompleteQuery != normalizedQuery) {
                            return@updateTransientState previous
                        }

                        previous.copy(
                            tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
                            isLoadingTagSuggestions = false,
                        )
                    }
                }
        }
    }

    private suspend fun persistDraftOnClearedIfNeeded(currentState: LinkDraftScreenState): Boolean {
        val draftKey = currentState.draftKey ?: return false
        if (currentState.isLoadingDraft) {
            return false
        }

        return linksRepository.updateLinkDraft(
            key = draftKey,
            request = currentState.toDismissSaveRequest(),
        ).fold(
            onSuccess = {
                if (currentState.photoKey != null && currentState.photoKey == ownedPhotoKey) {
                    ownedPhotoKey = null
                }
                true
            },
            onFailure = {
                logger.error("Failed to save add-link draft on clear key=$draftKey", it)
                false
            },
        )
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
                type = MediaPhotoType.Links,
            )
        }
    }

    private fun attachPhotoFromLocal(image: ComposerPickedImage) {
        uploadPhoto {
            mediaRepository.uploadPhotoFromDevice(
                bytes = image.bytes,
                fileName = image.fileName,
                mimeType = image.mimeType,
                type = MediaPhotoType.Links,
            )
        }
    }

    private fun uploadPhoto(upload: suspend () -> Result<Photo>) {
        val currentState = state.value
        if (currentState.isLoadingDraft || currentState.isMediaUploading || currentState.isPublishing) {
            return
        }

        val previousOwnedPhotoKey = ownedPhotoKey
        updateState { previous ->
            previous.copy(isMediaUploading = true)
        }

        viewModelScope.launch {
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
                    deletePhoto(previousOwnedPhotoKey)
                }
            }.onFailure {
                logger.error("Failed to upload add-link photo", it)
                updateState { previous ->
                    previous.copy(isMediaUploading = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private suspend fun cleanupCurrentOwnedPhotoImmediately() {
        val currentPhotoKey = state.value.photoKey ?: return
        if (currentPhotoKey != ownedPhotoKey) {
            return
        }

        ownedPhotoKey = null
        mediaRepository.deletePhoto(currentPhotoKey).onFailure {
            logger.error("Failed to delete add-link photo key=$currentPhotoKey", it)
        }
    }

    private fun deletePhoto(photoKey: String, showError: Boolean = false) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                mediaRepository.deletePhoto(photoKey).onFailure {
                    logger.error("Failed to delete add-link photo key=$photoKey", it)
                    if (showError) {
                        snackbarManager.tryEmitGenericError()
                    }
                }
            }
        }
    }

    private fun popAddLinkFlow() {
        while (appNavigator.state.value.rootStack.lastOrNull().isAddLinkScreen()) {
            if (!appNavigator.back()) {
                break
            }
        }
    }

    private fun restoreDraft(): RestoredAddLinkDraft? {
        val hasDraft = savedStateHandle.get<Boolean>(STATE_HAS_DRAFT) == true
        if (!hasDraft) {
            return null
        }

        return RestoredAddLinkDraft(
            state = LinkDraftScreenState(
                draftKey = savedStateHandle.get<String>(STATE_DRAFT_KEY),
                currentUrl = savedStateHandle.get<String>(STATE_CURRENT_URL).orEmpty(),
                title = savedStateHandle.get<String>(STATE_TITLE).orEmpty(),
                description = savedStateHandle.get<String>(STATE_DESCRIPTION).orEmpty(),
                tags = decodeTags(savedStateHandle.get<String>(STATE_TAGS)).toImmutableList(),
                tagInput = savedStateHandle.get<String>(STATE_TAG_INPUT).orEmpty(),
                tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
                suggestedImages = decodeSuggestedImages(savedStateHandle.get<String>(STATE_SUGGESTED_IMAGES)).toImmutableList(),
                selectedSuggestedImageIndex = savedStateHandle.get<Int>(STATE_SELECTED_SUGGESTED_IMAGE_INDEX),
                adult = savedStateHandle.get<Boolean>(STATE_ADULT) ?: false,
                photoKey = savedStateHandle.get<String>(STATE_PHOTO_KEY),
                photoUrl = savedStateHandle.get<String>(STATE_PHOTO_URL),
                isLoadingDraft = false,
                isLoadingTagSuggestions = false,
                isMediaUploading = false,
                isPublishing = false,
            ),
            ownedPhotoKey = savedStateHandle.get<String>(STATE_OWNED_PHOTO_KEY),
        )
    }

    private fun persistState(state: LinkDraftScreenState) {
        savedStateHandle[STATE_HAS_DRAFT] = state.toSnapshot() != AddLinkSnapshot.empty || state.draftKey != null
        savedStateHandle[STATE_DRAFT_KEY] = state.draftKey
        savedStateHandle[STATE_CURRENT_URL] = state.currentUrl
        savedStateHandle[STATE_TITLE] = state.title
        savedStateHandle[STATE_DESCRIPTION] = state.description
        savedStateHandle[STATE_TAGS] = encodeTags(state.tags)
        savedStateHandle[STATE_TAG_INPUT] = state.tagInput
        savedStateHandle[STATE_SUGGESTED_IMAGES] = json.encodeToString(
            ListSerializer(AddLinkSuggestedImageState.serializer()),
            state.suggestedImages.toList(),
        )
        savedStateHandle[STATE_SELECTED_SUGGESTED_IMAGE_INDEX] = state.selectedSuggestedImageIndex
        savedStateHandle[STATE_ADULT] = state.adult
        savedStateHandle[STATE_PHOTO_KEY] = state.photoKey
        savedStateHandle[STATE_PHOTO_URL] = state.photoUrl
        savedStateHandle[STATE_OWNED_PHOTO_KEY] = ownedPhotoKey
    }

    private fun clearPersistedState() {
        savedStateHandle.remove<Any?>(STATE_HAS_DRAFT)
        savedStateHandle.remove<Any?>(STATE_DRAFT_KEY)
        savedStateHandle.remove<Any?>(STATE_CURRENT_URL)
        savedStateHandle.remove<Any?>(STATE_TITLE)
        savedStateHandle.remove<Any?>(STATE_DESCRIPTION)
        savedStateHandle.remove<Any?>(STATE_TAGS)
        savedStateHandle.remove<Any?>(STATE_TAG_INPUT)
        savedStateHandle.remove<Any?>(STATE_SUGGESTED_IMAGES)
        savedStateHandle.remove<Any?>(STATE_SELECTED_SUGGESTED_IMAGE_INDEX)
        savedStateHandle.remove<Any?>(STATE_ADULT)
        savedStateHandle.remove<Any?>(STATE_PHOTO_KEY)
        savedStateHandle.remove<Any?>(STATE_PHOTO_URL)
        savedStateHandle.remove<Any?>(STATE_OWNED_PHOTO_KEY)
    }

    private inline fun updateState(transform: (LinkDraftScreenState) -> LinkDraftScreenState) {
        _state.update { previous ->
            transform(previous).also(::persistState)
        }
    }

    private inline fun updateTransientState(transform: (LinkDraftScreenState) -> LinkDraftScreenState) {
        _state.update(transform)
    }

    private fun isHttpUrl(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("http://") || normalized.startsWith("https://")
    }

    private fun decodeSuggestedImages(rawValue: String?): List<AddLinkSuggestedImageState> = rawValue
        ?.takeIf(String::isNotBlank)
        ?.let {
            runCatching {
                json.decodeFromString(ListSerializer(AddLinkSuggestedImageState.serializer()), it)
            }.getOrDefault(emptyList())
        }
        .orEmpty()

    private fun encodeTags(tags: List<String>): String = tags.joinToString(TAGS_SEPARATOR)

    private fun decodeTags(rawValue: String?): List<String> = rawValue
        ?.split(TAGS_SEPARATOR)
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        .orEmpty()

    private data class RestoredAddLinkDraft(val state: LinkDraftScreenState, val ownedPhotoKey: String?)

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }

        const val STATE_HAS_DRAFT = "add_link_has_draft"
        const val STATE_DRAFT_KEY = "add_link_draft_key"
        const val STATE_CURRENT_URL = "add_link_current_url"
        const val STATE_TITLE = "add_link_title"
        const val STATE_DESCRIPTION = "add_link_description"
        const val STATE_TAGS = "add_link_tags"
        const val STATE_TAG_INPUT = "add_link_tag_input"
        const val STATE_SUGGESTED_IMAGES = "add_link_suggested_images"
        const val STATE_SELECTED_SUGGESTED_IMAGE_INDEX = "add_link_selected_suggested_image_index"
        const val STATE_ADULT = "add_link_adult"
        const val STATE_PHOTO_KEY = "add_link_photo_key"
        const val STATE_PHOTO_URL = "add_link_photo_url"
        const val STATE_OWNED_PHOTO_KEY = "add_link_owned_photo_key"
        const val TAGS_SEPARATOR = "|"
        const val MIN_TAG_SUGGESTION_QUERY_LENGTH = 3
        const val TAG_SUGGESTION_DEBOUNCE_MILLIS = 300L
    }
}

private fun LinkDraftScreenState.toSnapshot(): AddLinkSnapshot = AddLinkSnapshot(
    currentUrl = currentUrl.trim(),
    title = title.trim(),
    description = description.trim(),
    tags = tags,
    tagInput = tagInput.trim(),
    selectedSuggestedImageIndex = selectedSuggestedImageIndex,
    adult = adult,
    photoKey = photoKey,
    photoUrl = photoUrl,
)

private fun Any?.isAddLinkScreen(): Boolean = this is AddLinkScreen ||
    this is LinkDraftScreen
