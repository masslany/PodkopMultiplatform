package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.embed.toTwitterEmbedPreviewState
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdate
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdatesStore
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entry.EntryVoteAction
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.entrycomment.toEntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.toLinkCommentItemState
import pl.masslany.podkop.features.resources.models.toDeletedByAuthor
import pl.masslany.podkop.features.resources.models.toResourceItemState
import pl.masslany.podkop.features.tag.TagScreen

open class BaseResourceItemStateHolder(
    private val linksRepository: LinksRepository,
    private val entriesRepository: EntriesRepository,
    private val favouritesRepository: FavouritesRepository,
    private val appNavigator: AppNavigator,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: AppLogger,
    private val twitterEmbedPreviewRepository: TwitterEmbedPreviewRepository,
    private val screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
    private val resourceActionUpdatesStore: ResourceActionUpdatesStore,
) : ResourceItemStateHolder {

    private val _items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    override val items = _items.asStateFlow()

    protected var scope: CoroutineScope? = null

    private var isUpcoming: Boolean = false
    private val updateMutex = Mutex()

    override fun init(
        scope: CoroutineScope,
        isUpcoming: Boolean,
    ) {
        this.scope = scope
        this.isUpcoming = isUpcoming
        observeResourceActionUpdates(scope)
    }

    private fun observeResourceActionUpdates(scope: CoroutineScope) {
        scope.launch {
            resourceActionUpdatesStore.updates.collect { update ->
                when (update) {
                    is ResourceActionUpdate.EntryCommentDeleted -> {
                        updateItem(update.commentId) { current ->
                            current.toDeletedByAuthor()
                        }
                    }

                    is ResourceActionUpdate.ResourceEdited -> {
                        applyResourceEdited(update.resource)
                    }
                }
            }
        }
    }

    override suspend fun updateData(data: List<ResourceItem>) {
        updateMutex.withLock {
            withContext(dispatcherProvider.default) {
                _items.value = data.map { it.toResourceItemState(isUpcoming) }.toImmutableList()
            }
        }
    }

    override suspend fun appendData(data: List<ResourceItem>) {
        updateMutex.withLock {
            withContext(dispatcherProvider.default) {
                _items.update { list ->
                    val existingIds = list
                        .mapTo(mutableSetOf()) { it.id }

                    val uniqueNewItems = data
                        .asSequence()
                        .filter { item -> existingIds.add(item.id) }
                        .map { item -> item.toResourceItemState(isUpcoming) }
                        .toList()

                    (list + uniqueNewItems).toImmutableList()
                }
            }
        }
    }

    override fun onLinkVoteClicked(id: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                linksRepository.removeVoteOnLink(linkId = id)
            } else {
                linksRepository.voteOnLink(linkId = id)
            }

            result.onSuccess {
                linksRepository.getLink(id)
                    .onSuccess {
                        logger.debug("Link updated: $it")
                        notifyItemUpdated(it.data)
                    }
                    .onFailure {
                        logger.error("Link update fetch failed for id=$id", it)
                    }
            }
        }
    }

    override fun onLinkFavouriteClicked(linkId: Int, favourited: Boolean) {
        scope?.launch {
            toggleFavourite(
                itemId = linkId,
                type = FavouriteType.Link,
                favourited = favourited,
            )
        }
    }

    override fun onLinkReplyClicked(linkId: Int, author: String?) = Unit

    override fun onLinkMoreClicked(linkId: Int, linkSlug: String) = Unit

    override fun onLinkClicked(id: Int) {
        appNavigator.navigateTo(LinkDetailsScreen(id))
    }

    override fun onLinkUrlClicked(url: String) {
        appNavigator.openExternalLink(url)
    }

    override fun onTagClicked(tag: String) {
        logger.debug("Tag clicked: $tag")
        appNavigator.navigateTo(TagScreen(tag = tag))
    }

    override fun onProfileClicked(username: String) {
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onImageClicked(url: String) {
        appNavigator.navigateTo(ImageViewerScreen(imageUrl = url))
    }

    override fun onEmbedPreviewClicked(
        itemId: Int,
        state: EmbedContentState,
    ) {
        when (state.type) {
            EmbedContentType.Youtube,
            EmbedContentType.Streamable,
            EmbedContentType.Other,
            -> {
                appNavigator.openExternalLink(state.url)
                return
            }

            EmbedContentType.Twitter -> {
                when (state.twitterState) {
                    null -> {
                        appNavigator.openExternalLink(state.url)
                    }

                    TwitterEmbedState.Loading -> Unit

                    is TwitterEmbedState.Loaded,
                    TwitterEmbedState.Error,
                    -> {
                        appNavigator.openExternalLink(state.url)
                    }

                    TwitterEmbedState.Preview -> {
                        scope?.launch {
                            updateTwitterEmbedState(
                                itemId = itemId,
                                embedKey = state.key,
                                newState = TwitterEmbedState.Loading,
                            )

                            val result = twitterEmbedPreviewRepository.getTweet(state.url)
                            result
                                .onSuccess { tweet ->
                                    updateTwitterEmbedState(
                                        itemId = itemId,
                                        embedKey = state.key,
                                        newState = TwitterEmbedState.Loaded(tweet.toTwitterEmbedPreviewState()),
                                    )
                                }
                                .onFailure { error ->
                                    logger.error(
                                        message = "Twitter embed preview fetch failed for itemId=$itemId " +
                                            "url=${state.url}",
                                        throwable = error,
                                    )
                                    updateTwitterEmbedState(
                                        itemId = itemId,
                                        embedKey = state.key,
                                        newState = TwitterEmbedState.Error,
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    override fun onEntryVoteUpClicked(entryId: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                entriesRepository.removeVoteUp(entryId)
            } else {
                entriesRepository.voteUp(entryId)
            }

            result.onSuccess {
                updateEntryVote(
                    entryId = entryId,
                    action = if (voted) {
                        EntryVoteAction.RemoveVoteUp
                    } else {
                        EntryVoteAction.VoteUp
                    },
                )
            }
        }
    }

    override fun onEntryFavouriteClicked(entryId: Int, favourited: Boolean) {
        scope?.launch {
            toggleFavourite(
                itemId = entryId,
                type = FavouriteType.Entry,
                favourited = favourited,
            )
        }
    }

    override fun onEntryClicked(id: Int) {
        appNavigator.navigateTo(EntryDetailsScreen.forEntry(id))
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        appNavigator.navigateTo(
            EntryDetailsScreen.forEntryReply(
                entryId = entryId,
                author = author,
            ),
        )
    }

    override fun onEntryMoreClicked(entryId: Int) {
        val draftId = createEntryScreenshotDraftId(entryId)
        val entry = _items.value
            .filterIsInstance<EntryItemState>()
            .firstOrNull { it.id == entryId }
        val canDelete = entry?.isDeleteEnabled == true
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forEntry(
                entryId = entryId,
                screenshotDraftId = draftId,
                canDelete = canDelete,
                canEdit = entry?.isEditEnabled == true,
                content = entry?.rawContent.orEmpty(),
                copyContent = entry
                    ?.takeIf { it.entryContentState is EntryContentState.Content && !it.isBlacklisted }
                    ?.rawContent
                    ?.takeIf(String::isNotBlank),
                adult = entry?.adult == true,
                photoKey = entry?.photoKey,
                photoUrl = entry?.photoUrl,
            ),
        )
    }

    override fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                entriesRepository.removeVoteUpComment(
                    entryId = parentEntryId,
                    commentId = entryCommentId,
                )
            } else {
                entriesRepository.voteUpComment(
                    entryId = parentEntryId,
                    commentId = entryCommentId,
                )
            }

            result.onSuccess {
                updateEntryCommentVote(
                    commentId = entryCommentId,
                    action = if (voted) {
                        EntryCommentVoteAction.RemoveVoteUp
                    } else {
                        EntryCommentVoteAction.VoteUp
                    },
                )
            }
        }
    }

    override fun onEntryCommentFavouriteClicked(entryCommentId: Int, favourited: Boolean) {
        scope?.launch {
            toggleFavourite(
                itemId = entryCommentId,
                type = FavouriteType.EntryComment,
                favourited = favourited,
            )
        }
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        appNavigator.navigateTo(
            EntryDetailsScreen.forEntryCommentReply(
                entryId = entryId,
                entryCommentId = entryCommentId,
                author = author,
            ),
        )
    }

    override fun onEntryCommentMoreClicked(entryId: Int, entryCommentId: Int) {
        val comment = _items.value
            .asSequence()
            .filterIsInstance<EntryCommentItemState>()
            .firstOrNull { it.id == entryCommentId }
            ?: _items.value
                .asSequence()
                .filterIsInstance<EntryItemState>()
                .firstOrNull { it.id == entryId }
                ?.comments
                ?.firstOrNull { it.id == entryCommentId }
        val canDelete = comment
            ?.isDeleteEnabled
            ?: false
        val draftId = createEntryCommentScreenshotDraftId(
            entryId = entryId,
            entryCommentId = entryCommentId,
        )
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forEntryComment(
                entryId = entryId,
                entryCommentId = entryCommentId,
                screenshotDraftId = draftId,
                canDelete = canDelete,
                canEdit = comment?.isEditEnabled == true,
                content = comment?.rawContent.orEmpty(),
                copyContent = comment
                    ?.takeIf { it.entryContentState is EntryContentState.Content && !it.isBlacklisted }
                    ?.rawContent
                    ?.takeIf(String::isNotBlank),
                adult = comment?.adult == true,
                photoKey = comment?.embedImageState?.key,
                photoUrl = comment?.embedImageState?.url,
            ),
        )
    }

    override fun onLinkCommentMoreClicked(
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    ) {
        val draftId = createTopLevelLinkCommentScreenshotDraftId(commentId)
        val comment = _items.value
            .filterIsInstance<LinkCommentItemState>()
            .firstOrNull { it.id == commentId }
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forLinkComment(
                linkId = linkId,
                linkSlug = linkSlug,
                linkCommentId = commentId,
                parentCommentId = parentCommentId,
                screenshotDraftId = draftId,
                canEdit = comment?.isEditEnabled == true,
                content = comment?.rawContent.orEmpty(),
                copyContent = comment
                    ?.takeIf { it.entryContentState is EntryContentState.Content && !it.isBlacklisted }
                    ?.rawContent
                    ?.takeIf(String::isNotBlank),
                adult = comment?.adult == true,
                photoKey = comment?.embedImageState?.key,
                photoUrl = comment?.embedImageState?.url,
            ),
        )
    }

    override fun onLinkCommentReplyClicked(linkId: Int, commentId: Int, author: String?) = Unit

    override fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                linksRepository.removeVoteOnLinkComment(
                    linkId = linkId,
                    commentId = commentId,
                )
            } else {
                linksRepository.voteOnLinkComment(
                    linkId = linkId,
                    commentId = commentId,
                )
            }

            result.onSuccess {
                updateLinkCommentVote(
                    commentId = commentId,
                    action = if (voted) {
                        LinkCommentVoteAction.RemoveVoteUp
                    } else {
                        LinkCommentVoteAction.VoteUp
                    },
                )
            }
        }
    }

    override fun onLinkCommentVoteDownClick(linkId: Int, commentId: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                linksRepository.removeVoteOnLinkComment(
                    linkId = linkId,
                    commentId = commentId,
                )
            } else {
                linksRepository.voteDownOnLinkComment(
                    linkId = linkId,
                    commentId = commentId,
                )
            }

            result.onSuccess {
                updateLinkCommentVote(
                    commentId = commentId,
                    action = if (voted) {
                        LinkCommentVoteAction.RemoveVoteDown
                    } else {
                        LinkCommentVoteAction.VoteDown
                    },
                )
            }
        }
    }

    override fun onLinkCommentFavouriteClicked(linkId: Int, commentId: Int, favourited: Boolean) {
        scope?.launch {
            toggleFavourite(
                itemId = commentId,
                type = FavouriteType.LinkComment,
                favourited = favourited,
            )
        }
    }

    private suspend fun toggleFavourite(
        itemId: Int,
        type: FavouriteType,
        favourited: Boolean,
    ) {
        val result = if (favourited) {
            favouritesRepository.deleteFavourite(type = type, sourceId = itemId)
        } else {
            favouritesRepository.createFavourite(type = type, sourceId = itemId)
        }

        result.onSuccess {
            updateItemFavourite(
                itemId = itemId,
                isFavourite = !favourited,
            )
        }.onFailure {
            logger.error("Failed to toggle favourite for itemId=$itemId type=$type", it)
        }
    }

    private fun updateEntryVote(
        entryId: Int,
        action: EntryVoteAction,
    ) {
        updateItem(entryId) { item ->
            val entry = item as? EntryItemState ?: return@updateItem item

            val newVoteState = when (action) {
                EntryVoteAction.VoteUp ->
                    entry.voteState.increaseVoteUp()

                EntryVoteAction.RemoveVoteUp ->
                    entry.voteState.removeVoteUp()
            }

            entry.copy(voteState = newVoteState)
        }
    }

    protected fun putScreenshotDraft(draft: ResourceScreenshotShareDraft): String =
        screenshotShareDraftStore.put(draft)

    private fun createEntryScreenshotDraftId(entryId: Int): String? {
        val entry = _items.value
            .filterIsInstance<EntryItemState>()
            .firstOrNull { it.id == entryId }
            ?: return null

        return putScreenshotDraft(
            ResourceScreenshotShareDraft.Entry(
                entry = entry,
            ),
        )
    }

    private fun createEntryCommentScreenshotDraftId(
        entryId: Int,
        entryCommentId: Int,
    ): String? {
        val directComment = _items.value
            .filterIsInstance<EntryCommentItemState>()
            .firstOrNull { it.id == entryCommentId }
        val nestedComment = _items.value
            .filterIsInstance<EntryItemState>()
            .firstOrNull { it.id == entryId }
            ?.comments
            ?.firstOrNull { it.id == entryCommentId }
        val comment = directComment ?: nestedComment ?: return null
        val parentEntry = _items.value
            .filterIsInstance<EntryItemState>()
            .firstOrNull { it.id == entryId }

        return putScreenshotDraft(
            ResourceScreenshotShareDraft.EntryComment(
                comment = comment,
                parentEntry = parentEntry,
            ),
        )
    }

    private fun createTopLevelLinkCommentScreenshotDraftId(commentId: Int): String? {
        val comment = _items.value
            .filterIsInstance<LinkCommentItemState>()
            .firstOrNull { it.id == commentId }
            ?: return null

        return putScreenshotDraft(
            ResourceScreenshotShareDraft.LinkComment(
                comment = comment,
            ),
        )
    }

    private fun updateLinkCommentVote(
        commentId: Int,
        action: LinkCommentVoteAction,
    ) {
        updateItem(commentId) { item ->
            val comment = item as? LinkCommentItemState ?: return@updateItem item

            val newVoteState = when (action) {
                LinkCommentVoteAction.VoteUp ->
                    comment.voteState.increaseVoteUp()

                LinkCommentVoteAction.RemoveVoteUp ->
                    comment.voteState.removeVoteUp()

                LinkCommentVoteAction.VoteDown ->
                    comment.voteState.increaseVoteDown()

                LinkCommentVoteAction.RemoveVoteDown ->
                    comment.voteState.removeVoteDown()
            }

            comment.copy(voteState = newVoteState)
        }
    }

    private fun updateEntryCommentVote(
        commentId: Int,
        action: EntryCommentVoteAction,
    ) {
        updateItem(commentId) { item ->
            val comment = item as? EntryCommentItemState ?: return@updateItem item

            val newVoteState = when (action) {
                EntryCommentVoteAction.VoteUp ->
                    comment.voteState.increaseVoteUp()

                EntryCommentVoteAction.RemoveVoteUp ->
                    comment.voteState.removeVoteUp()
            }

            comment.copy(voteState = newVoteState)
        }
    }

    private fun updateItemFavourite(
        itemId: Int,
        isFavourite: Boolean,
    ) {
        updateItem(itemId) { item ->
            when (item) {
                is EntryItemState -> item.copy(isFavourite = isFavourite)
                is EntryCommentItemState -> item.copy(isFavourite = isFavourite)
                is LinkItemState -> item.copy(isFavourite = isFavourite)
                is LinkCommentItemState -> item.copy(isFavourite = isFavourite)
                else -> item
            }
        }
    }

    protected fun updateItem(
        id: Int,
        updater: (ResourceItemState) -> ResourceItemState,
    ) {
        _items.update { list ->
            list.map { item ->
                mapItemWithNestedState(
                    item = item,
                    id = id,
                    updater = updater,
                )
            }.toImmutableList()
        }
    }

    private fun applyResourceEdited(resource: ResourceItem) {
        updateItem(resource.id) { existingState ->
            when (existingState) {
                is EntryCommentItemState -> {
                    resource.toEntryCommentItemState().copy(
                        parentId = existingState.parentId,
                    )
                }

                is LinkCommentItemState -> {
                    resource.toLinkCommentItemState(
                        linkIdOverride = existingState.linkId,
                        linkSlugOverride = existingState.linkSlug,
                    )
                }

                else -> resource.toResourceItemState(isUpcoming)
            }
        }
    }

    private fun updateTwitterEmbedState(
        itemId: Int,
        embedKey: String,
        newState: TwitterEmbedState,
    ) {
        updateItem(itemId) { item ->
            when (item) {
                is EntryItemState -> item.copy(
                    embedContentState = item.embedContentState
                        .updateTwitterEmbedStateIfMatches(embedKey = embedKey, newState = newState),
                )

                is EntryCommentItemState -> item.copy(
                    embedContentState = item.embedContentState
                        .updateTwitterEmbedStateIfMatches(embedKey = embedKey, newState = newState),
                )

                is LinkItemState -> item.copy(
                    embedContentState = item.embedContentState
                        .updateTwitterEmbedStateIfMatches(embedKey = embedKey, newState = newState),
                )

                is LinkCommentItemState -> item.copy(
                    embedContentState = item.embedContentState
                        .updateTwitterEmbedStateIfMatches(embedKey = embedKey, newState = newState),
                )

                else -> item
            }
        }
    }

    private fun mapItemWithNestedState(
        item: ResourceItemState,
        id: Int,
        updater: (ResourceItemState) -> ResourceItemState,
    ): ResourceItemState {
        val updatedItem = if (item.id == id) {
            updater(item)
        } else {
            item
        }

        return when (updatedItem) {
            is EntryItemState -> {
                updatedItem.copy(
                    comments = updatedItem.comments
                        .map { comment ->
                            mapItemWithNestedState(comment, id, updater) as? EntryCommentItemState ?: comment
                        }
                        .toImmutableList(),
                )
            }

            is LinkItemState -> {
                updatedItem.copy(
                    comments = updatedItem.comments
                        .map { comment ->
                            mapItemWithNestedState(comment, id, updater) as? LinkCommentItemState ?: comment
                        }
                        .toImmutableList(),
                )
            }

            is LinkCommentItemState -> {
                updatedItem.copy(
                    replies = updatedItem.replies
                        .map { reply ->
                            mapItemWithNestedState(reply, id, updater) as? LinkCommentItemState ?: reply
                        }
                        .toImmutableList(),
                )
            }

            else -> updatedItem
        }
    }

    // This is open so specialized handlers can update multiple lists
    override suspend fun notifyItemUpdated(newState: ResourceItem) {
        updateMutex.withLock {
            _items.update { list ->
                list.map {
                    if (it.id == newState.id) {
                        newState.toResourceItemState(isUpcoming)
                    } else {
                        it
                    }
                }.toImmutableList()
            }
        }
    }

    private enum class LinkCommentVoteAction {
        VoteUp,
        RemoveVoteUp,
        VoteDown,
        RemoveVoteDown,
    }

    private enum class EntryCommentVoteAction {
        VoteUp,
        RemoveVoteUp,
    }
}

internal fun EmbedContentState?.updateTwitterEmbedStateIfMatches(
    embedKey: String,
    newState: TwitterEmbedState,
): EmbedContentState? {
    val current = this ?: return null
    if (current.type != EmbedContentType.Twitter) return current
    if (current.key != embedKey) return current

    return current.copy(twitterState = newState)
}
