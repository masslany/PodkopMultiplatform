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
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.numberOrNull
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerPrefill
import pl.masslany.podkop.features.composer.ComposerRequest
import pl.masslany.podkop.features.composer.ComposerResult
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
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntryDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private val entryId = screen.id
    private val composerResultKeyPrefix = "entry-details-composer-$entryId-"
    private var isPendingComposerIntentConsumed = screen.pendingComposerIntent == null

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
        val page = request.numberOrNull() ?: run {
            logger.warn("Ignoring entry comments pagination request because numbered page was expected, got $request")
            return@Paginator Result.success(Resources(emptyList(), null))
        }

        entriesRepository.getEntryComments(
            page = page,
            entryId = entryId,
        )
    }

    private val _state = MutableStateFlow(initialState())
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        paginator.state,
    ) { state, comments, paginatorState ->
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
            isPaginating = paginatorState is PaginatorState.Loading,
        )
    }.stateIn(viewModelScope, WhileSubscribed(5000), initialState())

    init {
        resourceItemStateHolder.init(viewModelScope)
        observeComposerResults()
        loadContent(isRefreshing = false)
    }

    override fun onRefresh() {
        loadContent(isRefreshing = true)
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        if (entryId != this.entryId) return
        openEntryCommentComposer(author = author)
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        if (entryId != this.entryId) return
        openEntryCommentComposer(author = author)
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

    private fun openEntryCommentComposer(author: String?, canShowComposer: Boolean = _state.value.isLoggedIn) {
        if (!canShowComposer) {
            return
        }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefillText = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        val resultKey = "$composerResultKeyPrefix${kotlin.random.Random.nextInt()}"
        appNavigator.navigateTo(
            ComposerBottomSheetScreen(
                resultKey = resultKey,
                request = ComposerRequest.CreateEntryComment(
                    entryId = entryId,
                    prefill = ComposerPrefill(
                        content = prefillText,
                        replyTarget = if (normalizedAuthor.isEmpty()) null else "@$normalizedAuthor",
                    ),
                ),
            ),
        )
    }

    private fun observeComposerResults() {
        viewModelScope.launch {
            appNavigator.results.collect { (key, result) ->
                if (!key.startsWith(composerResultKeyPrefix)) {
                    return@collect
                }

                handleComposerResult(result)
            }
        }
    }

    private fun handleComposerResult(result: Any?) {
        val composerResult = result as? ComposerResult ?: return
        if (composerResult is ComposerResult.Submitted) {
            viewModelScope.launch {
                resourceItemStateHolder.appendData(listOf(composerResult.resource))
            }
        }
    }

    private fun applyViewerContext(viewerContext: ViewerContext) {
        updateState { previousState ->
            previousState.copy(
                isLoggedIn = viewerContext.isLoggedIn,
                currentUsername = viewerContext.username,
            )
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
            EntryComposerIntentType.Reply -> openEntryCommentComposer(
                author = screen.pendingComposerIntent.author,
                canShowComposer = canShowComposer,
            )

            null -> Unit
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

    private inline fun updateState(transform: (EntryDetailsScreenState) -> EntryDetailsScreenState) {
        _state.update { previousState ->
            transform(previousState)
        }
    }

    private fun initialState(): EntryDetailsScreenState = EntryDetailsScreenState.initial.copy(
        isLoading = true,
    )

    private data class ViewerContext(val isLoggedIn: Boolean, val username: String?)
}
