package pl.masslany.podkop.features.entrydetails

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
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
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val savedStateHandle: SavedStateHandle,
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
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.init entryId=$entryId pendingIntent=${screen.pendingComposerIntent} " +
                "restoredDraft=${restoredComposerDraft != null} consumed=$isPendingComposerIntentConsumed",
        )
        resourceItemStateHolder.init(viewModelScope)
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    override fun onComposerTextChanged(content: TextFieldValue) {
        updateState { previousState ->
            previousState.copy(composerContent = content)
        }
    }

    override fun onComposerAdultChanged(adult: Boolean) {
        updateState { previousState ->
            previousState.copy(composerAdult = adult)
        }
    }

    override fun onComposerDismissed() {
        updateState(::clearComposerState)
    }

    override fun onComposerSubmit() {
        val currentState = _state.value
        if (!currentState.isLoggedIn || !currentState.isComposerVisible || currentState.isComposerSubmitting) {
            return
        }

        val content = currentState.composerContent.text.trim()
        if (content.isBlank()) {
            return
        }

        updateState { previousState ->
            previousState.copy(isComposerSubmitting = true)
        }

        viewModelScope.launch {
            entriesRepository.createEntryComment(
                entryId = entryId,
                content = content,
                adult = currentState.composerAdult,
            ).onSuccess { createdResource ->
                resourceItemStateHolder.appendData(listOf(createdResource))
                updateState(::clearComposerState)
            }.onFailure {
                logger.error("Failed to submit entry reply composer for entryId=$entryId", it)
                updateState { previousState ->
                    previousState.copy(isComposerSubmitting = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.onEntryReplyClicked entryId=$entryId " +
                "currentEntryId=${this.entryId} author=${author.orEmpty()}",
        )
        if (entryId != this.entryId) return
        showComposerForAuthor(author = author)
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.onEntryCommentReplyClicked entryId=$entryId " +
                "entryCommentId=$entryCommentId currentEntryId=${this.entryId} author=${author.orEmpty()}",
        )
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

    private fun showComposerForAuthor(author: String?, canShowComposer: Boolean = _state.value.isLoggedIn) {
        if (!canShowComposer) {
            logger.debug(
                "[ReplyTrace] EntryDetailsViewModel.showComposerForAuthor skipped (not logged in) " +
                    "entryId=$entryId author=${author.orEmpty()}",
            )
            return
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefill = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        updateState { previousState ->
            previousState.copy(
                isComposerVisible = true,
                composerReplyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                composerContent = TextFieldValue(text = prefill, selection = TextRange(prefill.length)),
                composerAdult = false,
                isComposerSubmitting = false,
            )
        }
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.showComposerForAuthor applied entryId=$entryId " +
                "replyTarget=${if (normalizedAuthor.isEmpty()) "<none>" else "@$normalizedAuthor"} " +
                "prefillLength=${prefill.length}",
        )
    }

    private fun applyViewerContext(viewerContext: ViewerContext) {
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.applyViewerContext entryId=$entryId " +
                "isLoggedIn=${viewerContext.isLoggedIn} username=${viewerContext.username.orEmpty()}",
        )
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
        logger.debug(
            "[ReplyTrace] EntryDetailsViewModel.maybeApplyPendingComposerIntent entryId=$entryId " +
                "consumed=$isPendingComposerIntentConsumed canShowComposer=$canShowComposer " +
                "pendingIntent=${screen.pendingComposerIntent}",
        )
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
    ): EntryDetailsScreenState = previousState.copy(
        isComposerVisible = false,
        composerContent = TextFieldValue(),
        composerReplyTarget = null,
        composerAdult = false,
        isComposerSubmitting = false,
    )

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
            isComposerVisible = draft?.isVisible ?: false,
            composerContent = draft?.content ?: TextFieldValue(),
            composerReplyTarget = draft?.replyTarget,
            composerAdult = draft?.adult ?: false,
        )
    }

    private fun restoreComposerDraft(): RestoredComposerDraft? {
        val visible = savedStateHandle.get<Boolean>(STATE_COMPOSER_VISIBLE) ?: false
        val text = savedStateHandle.get<String>(STATE_COMPOSER_CONTENT).orEmpty()
        val selectionStart = savedStateHandle.get<Int>(STATE_COMPOSER_SELECTION_START) ?: text.length
        val selectionEnd = savedStateHandle.get<Int>(STATE_COMPOSER_SELECTION_END) ?: text.length
        val replyTarget = savedStateHandle.get<String>(STATE_COMPOSER_REPLY_TARGET)
        val adult = savedStateHandle.get<Boolean>(STATE_COMPOSER_ADULT) ?: false

        val hasPersistedDraft = visible || text.isNotEmpty() || replyTarget != null || adult
        if (!hasPersistedDraft) {
            return null
        }

        val clampedSelectionStart = selectionStart.coerceIn(0, text.length)
        val clampedSelectionEnd = selectionEnd.coerceIn(0, text.length)

        return RestoredComposerDraft(
            isVisible = visible || text.isNotEmpty(),
            content = TextFieldValue(
                text = text,
                selection = TextRange(clampedSelectionStart, clampedSelectionEnd),
            ),
            replyTarget = replyTarget,
            adult = adult,
        )
    }

    private fun persistComposerDraft(state: EntryDetailsScreenState) {
        val hasPersistedDraft = state.isComposerVisible ||
            state.composerContent.text.isNotEmpty() ||
            state.composerReplyTarget != null ||
            state.composerAdult

        if (!hasPersistedDraft) {
            savedStateHandle.remove<Any?>(STATE_COMPOSER_VISIBLE)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_CONTENT)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_START)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_SELECTION_END)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_REPLY_TARGET)
            savedStateHandle.remove<Any?>(STATE_COMPOSER_ADULT)
            return
        }

        savedStateHandle[STATE_COMPOSER_VISIBLE] = state.isComposerVisible
        savedStateHandle[STATE_COMPOSER_CONTENT] = state.composerContent.text
        savedStateHandle[STATE_COMPOSER_SELECTION_START] = state.composerContent.selection.start
        savedStateHandle[STATE_COMPOSER_SELECTION_END] = state.composerContent.selection.end
        savedStateHandle[STATE_COMPOSER_REPLY_TARGET] = state.composerReplyTarget
        savedStateHandle[STATE_COMPOSER_ADULT] = state.composerAdult
    }

    private data class ViewerContext(val isLoggedIn: Boolean, val username: String?)

    private data class RestoredComposerDraft(
        val isVisible: Boolean,
        val content: TextFieldValue,
        val replyTarget: String?,
        val adult: Boolean,
    )

    private companion object {
        const val STATE_COMPOSER_VISIBLE = "entry_details_composer_visible"
        const val STATE_COMPOSER_CONTENT = "entry_details_composer_content"
        const val STATE_COMPOSER_SELECTION_START = "entry_details_composer_selection_start"
        const val STATE_COMPOSER_SELECTION_END = "entry_details_composer_selection_end"
        const val STATE_COMPOSER_REPLY_TARGET = "entry_details_composer_reply_target"
        const val STATE_COMPOSER_ADULT = "entry_details_composer_adult"
    }
}
