package pl.masslany.podkop.business.entries.domain.models

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

/** An entry with its comments as a reply tree (the `entries-threads` API). */
data class EntryThread(
    val entry: ResourceItem,
    val comments: EntryThreadReplies,
)

/**
 * A loaded slice of one node's direct replies. [totalCount] counts every direct reply on the
 * server, so replies beyond [items] can still be fetched with the last item's id as the cursor.
 */
data class EntryThreadReplies(
    val totalCount: Int,
    val items: List<EntryThreadComment>,
) {
    val remainingCount: Int get() = (totalCount - items.size).coerceAtLeast(0)
}

data class EntryThreadComment(
    val comment: ResourceItem,
    /** 0 for comments directly under the entry, +1 per reply level. */
    val depth: Int,
    val isByEntryAuthor: Boolean,
    /**
     * The comment a reply to this one is posted under. Past the server's depth limit replies go
     * to this comment's parent.
     */
    val replyParentId: Int,
    val replies: EntryThreadReplies,
)

enum class EntryThreadSort(val value: String) {
    Best("best"),
    Oldest("oldest"),
}
