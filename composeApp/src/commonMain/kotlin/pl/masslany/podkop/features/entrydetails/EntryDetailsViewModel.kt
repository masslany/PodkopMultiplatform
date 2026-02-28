package pl.masslany.podkop.features.entrydetails

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
    private val id: Int,
    private val entriesRepository: EntriesRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val resourceItemStateHolder: ResourceItemStateHolder,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntryDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private var entryResource: ResourceItem? = null

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated entry comments for id=$id", it)
            snackbarManager.tryEmitGenericError()
        },
    ) { request ->
        entriesRepository.getEntryComments(
            page = when (request) {
                is PageRequest.Index -> request.page
                is PageRequest.Cursor -> request.key
            },
            entryId = id,
        )
    }

    private val _state = MutableStateFlow(EntryDetailsScreenState.initial)
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, comments, paginator ->
        logger.debug("Entry details comments updated: $comments")
        val holderEntry = comments
            .filterIsInstance<EntryItemState>()
            .firstOrNull { it.id == id }
        val holderComments = comments
            .filterNot { item -> item is EntryItemState && item.id == id }
            .toImmutableList()
        state.copy(
            entry = holderEntry ?: state.entry,
            comments = holderComments,
            isPaginating = paginator is PaginatorState.Loading,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), EntryDetailsScreenState.initial)

    init {
        resourceItemStateHolder.init(viewModelScope)
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    override fun onComposerTextChanged(content: String) {
        _state.update { previousState ->
            previousState.copy(composerContent = content)
        }
    }

    override fun onComposerDismissed() {
        _state.update { previousState ->
            previousState.copy(
                isComposerVisible = false,
                composerContent = "",
                composerReplyTarget = null,
                isComposerSubmitting = false,
            )
        }
    }

    override fun onComposerSubmit() {
        val currentState = state.value
        if (!currentState.isLoggedIn || !currentState.isComposerVisible || currentState.isComposerSubmitting) {
            return
        }

        val content = currentState.composerContent.trim()
        if (content.isBlank()) {
            return
        }

        _state.update { previousState ->
            previousState.copy(isComposerSubmitting = true)
        }

        viewModelScope.launch {
            entriesRepository.createEntryComment(
                entryId = id,
                content = content,
                adult = false,
            ).onSuccess { comment ->
                resourceItemStateHolder.appendData(listOf(comment))
                _state.update { previousState ->
                    previousState.copy(
                        isComposerVisible = false,
                        composerContent = "",
                        composerReplyTarget = null,
                        isComposerSubmitting = false,
                    )
                }
            }.onFailure {
                logger.error("Failed to create entry comment for id=$id", it)
                _state.update { previousState ->
                    previousState.copy(isComposerSubmitting = false)
                }
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        if (entryId != id) return
        showComposerForAuthor(author)
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        if (entryId != id) return
        showComposerForAuthor(author)
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
                    entriesRepository.getEntry(entryId = id)
                }
                val commentsDeferred = async {
                    entriesRepository.getEntryComments(
                        entryId = id,
                        page = 1,
                    )
                }

                val isEntryLoaded = entryDeferred.await()
                    .onSuccess {
                        entryResource = it
                        resourceItemStateHolder.updateData(listOf(it))
                        _state.update { previousState ->
                            previousState.copy(entry = it.toResourceItemState())
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry details for id=$id", it)
                    }
                    .isSuccess

                viewerContextDeferred.await().let { viewerContext ->
                    _state.update { previousState ->
                        previousState.copy(
                            isLoggedIn = viewerContext.isLoggedIn,
                            currentUsername = viewerContext.username,
                            isComposerVisible = if (viewerContext.isLoggedIn) {
                                previousState.isComposerVisible
                            } else {
                                false
                            },
                            composerContent = if (viewerContext.isLoggedIn) {
                                previousState.composerContent
                            } else {
                                ""
                            },
                            composerReplyTarget = if (viewerContext.isLoggedIn) {
                                previousState.composerReplyTarget
                            } else {
                                null
                            },
                            isComposerSubmitting = if (viewerContext.isLoggedIn) {
                                previousState.isComposerSubmitting
                            } else {
                                false
                            },
                        )
                    }
                }

                commentsDeferred.await()
                    .onSuccess { comments ->
                        resourceItemStateHolder.updateData(topLevelEntryAndComments(comments.data))
                        paginator.setup(comments.pagination, comments.data.size)
                        _state.update { previousState ->
                            previousState.updateCommentsError(false)
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry comments for id=$id", it)
                        resourceItemStateHolder.updateData(topLevelEntryAndComments(emptyList()))
                        _state.update { previousState ->
                            previousState.updateCommentsError(true)
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                _state.update { previousState ->
                    previousState.updateError(!isEntryLoaded)
                }
            }

            _state.update { previousState ->
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

    private fun showComposerForAuthor(author: String?) {
        if (!state.value.isLoggedIn) {
            return
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefill = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        _state.update { previousState ->
            previousState.copy(
                isComposerVisible = true,
                composerReplyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                composerContent = prefill,
                isComposerSubmitting = false,
            )
        }
    }

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

    private data class ViewerContext(val isLoggedIn: Boolean, val username: String?)
}
