package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.entrycomment.toEntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.toLinkCommentItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState
import pl.masslany.podkop.features.resources.models.withEmbedContentState

/**
 * The item states behind a [ResourceItemStateHolderImpl]. Updates by id also reach the comments
 * nested in entries and links, and the replies nested in link comments.
 */
internal class ResourceItemsStore(
    private val dispatcherProvider: DispatcherProvider,
    private val isUpcoming: Boolean,
) {
    private val _items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    val items: StateFlow<ImmutableList<ResourceItemState>> = _items.asStateFlow()

    private val mutex = Mutex()

    suspend fun replaceAll(data: List<ResourceItem>) {
        mutex.withLock {
            withContext(dispatcherProvider.default) {
                _items.value = data.map { it.toResourceItemState(isUpcoming) }.toImmutableList()
            }
        }
    }

    suspend fun append(data: List<ResourceItem>) {
        mutex.withLock {
            withContext(dispatcherProvider.default) {
                _items.update { list ->
                    list.appendPage(data) { item -> item.toResourceItemState(isUpcoming) }
                }
            }
        }
    }

    /** Swaps the top-level item with [resource]'s id for a fresh state; nested comments are left alone. */
    suspend fun replace(resource: ResourceItem) {
        mutex.withLock {
            _items.update { list ->
                list.map {
                    if (it.id == resource.id) resource.toResourceItemState(isUpcoming) else it
                }.toImmutableList()
            }
        }
    }

    fun update(id: Int, updater: (ResourceItemState) -> ResourceItemState) {
        _items.update { list ->
            list.map { it.updateById(id, updater) }.toImmutableList()
        }
    }

    inline fun <reified T : ResourceItemState> updateAs(id: Int, crossinline transform: (T) -> T) {
        update(id) { item -> if (item is T) transform(item) else item }
    }

    fun updateEmbed(itemId: Int, transform: (EmbedContentState?) -> EmbedContentState?) {
        update(itemId) { it.withEmbedContentState(transform) }
    }

    fun applyEdited(resource: ResourceItem) {
        update(resource.id) { existingState ->
            when (existingState) {
                is EntryCommentItemState -> resource.toEntryCommentItemState().copy(
                    parentId = existingState.parentId,
                )

                is LinkCommentItemState -> resource.toLinkCommentItemState(
                    linkIdOverride = existingState.linkId,
                    linkSlugOverride = existingState.linkSlug,
                )

                else -> resource.toResourceItemState(isUpcoming)
            }
        }
    }

    fun findEntry(id: Int): EntryItemState? = findTopLevel(id)

    /** Finds a listed entry comment, or one shown under its entry. */
    fun findEntryComment(entryId: Int, commentId: Int): EntryCommentItemState? =
        findTopLevel<EntryCommentItemState>(commentId)
            ?: findEntry(entryId)?.comments?.firstOrNull { it.id == commentId }

    fun findLinkComment(id: Int): LinkCommentItemState? = findTopLevel(id)

    private inline fun <reified T : ResourceItemState> findTopLevel(id: Int): T? =
        _items.value.firstOrNull { it is T && it.id == id } as T?
}

private fun ResourceItemState.updateById(
    id: Int,
    updater: (ResourceItemState) -> ResourceItemState,
): ResourceItemState {
    val updatedItem = if (this.id == id) updater(this) else this

    return when (updatedItem) {
        is EntryItemState -> updatedItem.copy(
            comments = updatedItem.comments
                .map { comment -> comment.updateById(id, updater) as? EntryCommentItemState ?: comment }
                .toImmutableList(),
        )

        is LinkItemState -> updatedItem.copy(
            comments = updatedItem.comments
                .map { comment -> comment.updateById(id, updater) as? LinkCommentItemState ?: comment }
                .toImmutableList(),
        )

        is LinkCommentItemState -> updatedItem.copy(
            replies = updatedItem.replies
                .map { reply -> reply.updateById(id, updater) as? LinkCommentItemState ?: reply }
                .toImmutableList(),
        )

        else -> updatedItem
    }
}

/**
 * Adds a later page's items, skipping any already listed: feeds repeat items across pages, e.g. the
 * homepage repeats its promoted link and entries on every page.
 */
internal fun List<ResourceItemState>.appendPage(
    page: List<ResourceItem>,
    toState: (ResourceItem) -> ResourceItemState,
): ImmutableList<ResourceItemState> {
    val existingIds = mapTo(mutableSetOf()) { it.id }
    val uniqueNewItems = page
        .asSequence()
        .filter { item -> existingIds.add(item.id) }
        .map(toState)
        .toList()
    return (this + uniqueNewItems).toImmutableList()
}
