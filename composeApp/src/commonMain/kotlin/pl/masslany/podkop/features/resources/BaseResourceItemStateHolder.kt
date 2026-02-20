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
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entry.EntryVoteAction
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState
import pl.masslany.podkop.features.tag.TagScreen

open class BaseResourceItemStateHolder(
    private val linksRepository: LinksRepository,
    private val entriesRepository: EntriesRepository,
    private val appNavigator: AppNavigator,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: AppLogger,
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

    override fun onEntryClicked(id: Int) {
        appNavigator.navigateTo(EntryDetailsScreen(id))
    }

    override fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean) {
    }

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
            }

            comment.copy(voteState = newVoteState)
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
    open suspend fun notifyItemUpdated(newState: ResourceItem) {
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
    }
}
