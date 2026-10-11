package pl.masslany.podkop.features.entrydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.business.entries.domain.models.EntryThreadSort
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.pagination.Paginator
import pl.masslany.podkop.common.pagination.PaginatorState
import pl.masslany.podkop.common.pagination.numberOrNull
import pl.masslany.podkop.common.settings.AppSettings
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
    private val appSettings: AppSettings,
    topBarActions: TopBarActions,
) : ViewModel(),
    EntryDetailsActions,
    TopBarActions by topBarActions,
    ResourceItemStateHolder by resourceItemStateHolder {

    private val entryId = screen.id
    private val composerResultKeyPrefix = "entry-details-composer-$entryId-"
    private var isPendingComposerIntentConsumed = screen.pendingComposerIntent == null

    private var entryResource: ResourceItem? = null

    // Threaded mode only: null while comments show as the flat list.
    private val threadTree = MutableStateFlow<EntryCommentThreadTree?>(null)
    private val loadingRepliesIds = MutableStateFlow<Set<Int>>(emptySet())
    private val isLoadingMoreTopLevel = MutableStateFlow(false)

    // Set when more top-level comments failed to load: scrolling stops asking for them, and the
    // list offers a retry instead.
    private val isTopLevelPaginationFailed = MutableStateFlow(false)

    // Parent of every load for the content on screen. A reload cancels it, so requests from the
    // previous load stop instead of landing on the new content.
    private var contentLoads = SupervisorJob(viewModelScope.coroutineContext.job)

    // Composer result key -> comment the reply was posted under (null = top level), threaded mode only.
    private val pendingReplyParents = mutableMapOf<String, Int?>()

    private val paginator = Paginator(
        scope = viewModelScope,
        onNewItems = { data ->
            resourceItemStateHolder.appendData(data)
        },
        onError = {
            logger.error("Failed to load paginated entry comments for id=$entryId", it)
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

    private val pagination = combine(
        paginator.state,
        isLoadingMoreTopLevel,
        isTopLevelPaginationFailed,
    ) { paginatorState, isLoadingTopLevel, isTopLevelFailed ->
        PaginationStatus(
            isPaginating = paginatorState is PaginatorState.Loading || isLoadingTopLevel,
            isError = paginatorState is PaginatorState.Error || isTopLevelFailed,
        )
    }

    private val _state = MutableStateFlow(initialState())
    val state = combine(
        _state,
        resourceItemStateHolder.items,
        pagination,
        threadTree,
        loadingRepliesIds,
    ) { state, comments, pagination, tree, loadingReplies ->
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
            isPaginating = pagination.isPaginating,
            isPaginationError = pagination.isError,
            threadRows = tree?.let { buildEntryThreadRows(it, holderComments, loadingReplies) },
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
        openEntryCommentComposer(author = author, replyToCommentId = entryCommentId)
    }

    override fun onShowMoreEntryRepliesClicked(parentCommentId: Int) {
        val tree = threadTree.value ?: return
        if (parentCommentId in loadingRepliesIds.value) return
        loadingRepliesIds.update { it + parentCommentId }

        launchContentLoad {
            entriesRepository.getEntryThreadReplies(
                entryId = entryId,
                parentCommentId = parentCommentId,
                sort = THREAD_SORT,
                afterId = tree.repliesCursorId(parentCommentId),
            )
                .onSuccess { page -> appendThreadPage(parentCommentId, page) }
                .onFailure {
                    logger.error("Failed to load replies to entry comment id=$parentCommentId", it)
                    snackbarManager.tryEmitGenericError()
                }
            loadingRepliesIds.update { it - parentCommentId }
        }
    }

    private fun loadContent(isRefreshing: Boolean) {
        contentLoads.cancel()
        contentLoads = SupervisorJob(viewModelScope.coroutineContext.job)
        loadingRepliesIds.value = emptySet()
        isLoadingMoreTopLevel.value = false
        isTopLevelPaginationFailed.value = false

        _state.update { previousState ->
            previousState
                .updateLoading(!isRefreshing)
                .updateError(false)
                .updateCommentsError(false)
                .updateRefreshing(isRefreshing)
        }

        launchContentLoad {
            coroutineScope {
                val viewerContextDeferred = async {
                    resolveViewerContext()
                }
                val entryDeferred = async {
                    entriesRepository.getEntry(entryId = entryId)
                }
                val commentsDeferred = async {
                    loadComments(threaded = appSettings.threadedEntryComments.first())
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

                val viewerContext = viewerContextDeferred.await()
                applyViewerContext(viewerContext)

                commentsDeferred.await()
                    .onSuccess { comments ->
                        when (comments) {
                            is LoadedComments.Flat -> {
                                threadTree.value = null
                                resourceItemStateHolder.updateData(topLevelEntryAndComments(comments.resources.data))
                                paginator.setup(comments.resources.pagination, comments.resources.data.size)
                            }

                            is LoadedComments.Threaded -> {
                                // Tree first, so the thread comments never render as a flat list.
                                threadTree.value = EntryCommentThreadTree.from(comments.thread.comments)
                                resourceItemStateHolder.updateData(
                                    topLevelEntryAndComments(comments.thread.comments.flattenComments()),
                                )
                            }
                        }
                        updateState { previousState ->
                            previousState.updateCommentsError(false)
                        }
                    }
                    .onFailure {
                        logger.error("Failed to load entry comments for id=$entryId", it)
                        threadTree.value = null
                        resourceItemStateHolder.updateData(topLevelEntryAndComments(emptyList()))
                        updateState { previousState ->
                            previousState.updateCommentsError(true)
                        }
                        snackbarManager.tryEmitGenericError()
                    }

                updateState { previousState ->
                    previousState.updateError(!isEntryLoaded)
                }

                // After comments, so a reply to a comment can be nested under it in threaded mode.
                maybeApplyPendingComposerIntent(canShowComposer = viewerContext.isLoggedIn)
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
    ): Boolean {
        val tree = threadTree.value ?: return paginator.shouldPaginate(lastVisibleIndex, totalItems)
        if (lastVisibleIndex == null || isLoadingMoreTopLevel.value || isTopLevelPaginationFailed.value) return false
        return tree.hasMoreTopLevel && lastVisibleIndex + THREAD_PREFETCH_DISTANCE >= totalItems
    }

    override fun paginate() {
        if (threadTree.value == null) {
            paginator.paginate()
        } else {
            loadMoreTopLevelComments()
        }
    }

    private suspend fun loadComments(threaded: Boolean): Result<LoadedComments> {
        if (threaded) {
            entriesRepository.getEntryThread(entryId = entryId, sort = THREAD_SORT)
                .onSuccess { return Result.success(LoadedComments.Threaded(it)) }
                .onFailure { logger.warn("Falling back to flat entry comments for id=$entryId", it) }
        }
        return entriesRepository.getEntryComments(entryId = entryId, page = 1)
            .map { LoadedComments.Flat(it) }
    }

    private fun loadMoreTopLevelComments() {
        val tree = threadTree.value ?: return
        if (!isLoadingMoreTopLevel.compareAndSet(expect = false, update = true)) return
        isTopLevelPaginationFailed.value = false

        launchContentLoad {
            entriesRepository.getEntryThreadReplies(
                entryId = entryId,
                parentCommentId = null,
                sort = THREAD_SORT,
                afterId = tree.topLevelCursorId,
            )
                .onSuccess { page -> appendThreadPage(parentId = null, page = page) }
                .onFailure {
                    logger.error("Failed to load more entry comments for id=$entryId", it)
                    // Stop auto-paginating into the same error on every scroll; the list offers a retry.
                    isTopLevelPaginationFailed.value = true
                }
            isLoadingMoreTopLevel.value = false
        }
    }

    private fun launchContentLoad(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(contentLoads, block = block)
    }

    private suspend fun appendThreadPage(parentId: Int?, page: EntryThreadReplies) {
        resourceItemStateHolder.appendData(page.flattenComments())
        threadTree.update { it?.appendPage(parentId, page) }
    }

    private fun openEntryCommentComposer(
        author: String?,
        replyToCommentId: Int? = null,
        canShowComposer: Boolean = _state.value.isLoggedIn,
    ) {
        if (!canShowComposer) {
            return
        }

        val tree = threadTree.value
        val parentCommentId = replyToCommentId?.let { tree?.replyParentId(it) }

        val normalizedAuthor = author?.trim().orEmpty()
        val prefillText = if (normalizedAuthor.isEmpty()) {
            ""
        } else {
            "@$normalizedAuthor: "
        }

        val resultKey = "$composerResultKeyPrefix${kotlin.random.Random.nextInt()}"
        if (tree != null) {
            pendingReplyParents[resultKey] = parentCommentId
        }
        appNavigator.navigateTo(
            ComposerBottomSheetScreen(
                resultKey = resultKey,
                request = ComposerRequest.CreateEntryComment(
                    entryId = entryId,
                    parentCommentId = parentCommentId,
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

                handleComposerResult(key, result)
            }
        }
    }

    private fun handleComposerResult(key: String, result: Any?) {
        val composerResult = result as? ComposerResult ?: return
        val isThreadedReply = key in pendingReplyParents
        val parentCommentId = pendingReplyParents.remove(key)
        if (composerResult is ComposerResult.Submitted) {
            viewModelScope.launch {
                val resource = composerResult.resource
                resourceItemStateHolder.appendData(listOf(resource))
                if (isThreadedReply) {
                    val entryAuthor = entryResource?.author?.username
                    threadTree.update { tree ->
                        tree?.appendPosted(
                            parentId = parentCommentId,
                            commentId = resource.id,
                            isByEntryAuthor = entryAuthor != null && resource.author?.username == entryAuthor,
                        )
                    }
                }
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
                replyToCommentId = screen.pendingComposerIntent.entryCommentId,
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

    private sealed interface LoadedComments {
        data class Flat(val resources: Resources) : LoadedComments

        data class Threaded(val thread: EntryThread) : LoadedComments
    }

    private companion object {
        // Chronological like the flat list; the service default is "best".
        val THREAD_SORT = EntryThreadSort.Oldest
        const val THREAD_PREFETCH_DISTANCE = 8
    }
}

private data class PaginationStatus(
    val isPaginating: Boolean,
    val isError: Boolean,
)
