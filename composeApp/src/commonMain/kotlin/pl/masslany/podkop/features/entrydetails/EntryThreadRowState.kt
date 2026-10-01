package pl.masslany.podkop.features.entrydetails

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.features.resources.models.ResourceItemState

/** One row of threaded entry comments, in display order. */
@Immutable
sealed interface EntryThreadRowState {
    val key: String
    val depth: Int

    val connectors: ThreadConnectors

    data class Comment(
        val item: ResourceItemState,
        override val depth: Int,
        val isByEntryAuthor: Boolean,
        override val connectors: ThreadConnectors = ThreadConnectors(),
    ) : EntryThreadRowState {
        override val key: String get() = "comment-${item.id}"
    }

    data class MoreReplies(
        val parentId: Int,
        override val depth: Int,
        val remainingCount: Int,
        val isLoading: Boolean,
        override val connectors: ThreadConnectors = ThreadConnectors(),
    ) : EntryThreadRowState {
        override val key: String get() = "more-$parentId"
    }
}

/** Resolves [tree] rows against the holder's comment states; rows without a loaded state are skipped. */
internal fun buildEntryThreadRows(
    tree: EntryCommentThreadTree,
    comments: List<ResourceItemState>,
    loadingRepliesIds: Set<Int>,
): ImmutableList<EntryThreadRowState> {
    val commentsById = comments.associateBy { it.id }
    return tree.rows().mapNotNull { row ->
        when (row) {
            is EntryCommentThreadTree.Row.Comment -> commentsById[row.id]?.let { item ->
                EntryThreadRowState.Comment(
                    item = item,
                    depth = row.depth,
                    isByEntryAuthor = row.isByEntryAuthor,
                    connectors = row.connectors,
                )
            }

            is EntryCommentThreadTree.Row.MoreReplies -> EntryThreadRowState.MoreReplies(
                parentId = row.parentId,
                depth = row.depth,
                remainingCount = row.remainingCount,
                isLoading = row.parentId in loadingRepliesIds,
                connectors = row.connectors,
            )
        }
    }.toImmutableList()
}
