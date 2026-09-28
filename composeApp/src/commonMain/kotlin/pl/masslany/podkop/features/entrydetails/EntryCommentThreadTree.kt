package pl.masslany.podkop.features.entrydetails

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies

/**
 * The shape of an entry's threaded comments: which comment sits under which, how deep, and how
 * many replies each branch still has on the server. The comment contents themselves live in the
 * resource item holder, keyed by id.
 */
internal data class EntryCommentThreadTree(
    private val root: Branch,
    private val nodes: Map<Int, Node>,
) {
    /** Loaded children plus what is needed to fetch the rest after them. */
    data class Branch(
        val childIds: List<Int>,
        val totalCount: Int,
        /** Id of the last child loaded from the server; the next page starts after it. */
        val cursorId: Int?,
    ) {
        val remainingCount: Int get() = (totalCount - childIds.size).coerceAtLeast(0)
    }

    data class Node(
        val depth: Int,
        val isByEntryAuthor: Boolean,
        val replyParentId: Int,
        val replies: Branch,
    )

    sealed interface Row {
        val connectors: ThreadConnectors

        data class Comment(
            val id: Int,
            val depth: Int,
            val isByEntryAuthor: Boolean,
            override val connectors: ThreadConnectors = ThreadConnectors(),
        ) : Row

        data class MoreReplies(
            val parentId: Int,
            val depth: Int,
            val remainingCount: Int,
            override val connectors: ThreadConnectors = ThreadConnectors(),
        ) : Row
    }

    val hasMoreTopLevel: Boolean get() = root.remainingCount > 0

    /** Cursor for the next page of top-level comments. */
    val topLevelCursorId: Int? get() = root.cursorId

    fun repliesCursorId(parentId: Int): Int? = nodes[parentId]?.replies?.cursorId

    /** The comment a reply to [commentId] must be posted under, or null when it is not loaded. */
    fun replyParentId(commentId: Int): Int? = nodes[commentId]?.replyParentId

    /** Adds a page loaded from the server under [parentId], or under the entry when it is null. */
    fun appendPage(parentId: Int?, page: EntryThreadReplies): EntryCommentThreadTree {
        val branch = branchOf(parentId) ?: return this
        val newNodes = nodes.toMutableMap()
        val newChildIds = page.items
            .filter { it.comment.id !in branch.childIds }
            .onEach { newNodes.register(it) }
            .map { it.comment.id }
        val updatedBranch = Branch(
            childIds = branch.childIds + newChildIds,
            totalCount = maxOf(page.totalCount, branch.childIds.size + newChildIds.size),
            cursorId = page.items.lastOrNull()?.comment?.id ?: branch.cursorId,
        )
        return copy(nodes = newNodes).withBranch(parentId, updatedBranch)
    }

    /**
     * Adds a comment the user just posted as the last child of [parentId]. It does not move the
     * server cursor, so older unloaded siblings still load before it.
     */
    fun appendPosted(parentId: Int?, commentId: Int, isByEntryAuthor: Boolean): EntryCommentThreadTree {
        if (commentId in nodes) return this
        val branch = branchOf(parentId) ?: return this
        val depth = parentId?.let { nodes.getValue(it).depth + 1 } ?: 0
        val node = Node(
            depth = depth,
            isByEntryAuthor = isByEntryAuthor,
            replyParentId = if (depth >= MAX_REPLY_DEPTH) parentId ?: commentId else commentId,
            replies = Branch(childIds = emptyList(), totalCount = 0, cursorId = null),
        )
        val newNodes = nodes + (commentId to node)
        val updatedBranch = branch.copy(
            childIds = branch.childIds + commentId,
            totalCount = branch.totalCount + 1,
        )
        return copy(nodes = newNodes).withBranch(parentId, updatedBranch)
    }

    /** Depth-first rows, with a "more replies" row after each branch that has unloaded replies. */
    fun rows(): List<Row> = buildList {
        addRows(childIds = root.childIds, branchHasMore = false, ancestorLines = emptyList())
    }

    private fun MutableList<Row>.addRows(childIds: List<Int>, branchHasMore: Boolean, ancestorLines: List<Boolean>) {
        val loadedIds = childIds.filter { it in nodes }
        loadedIds.forEachIndexed { index, id ->
            val node = nodes.getValue(id)
            val replies = node.replies
            // Top-level comments hang from no line; the entry itself has no connector.
            val continuesBelow = node.depth > 0 && (index < loadedIds.lastIndex || branchHasMore)
            add(
                Row.Comment(
                    id = id,
                    depth = node.depth,
                    isByEntryAuthor = node.isByEntryAuthor,
                    connectors = ThreadConnectors(
                        ancestorLines = ancestorLines,
                        continuesBelow = continuesBelow,
                        hasReplies = replies.childIds.isNotEmpty() || replies.remainingCount > 0,
                    ),
                ),
            )
            val childAncestorLines = if (node.depth == 0) emptyList() else ancestorLines + continuesBelow
            addRows(childIds = replies.childIds, branchHasMore = replies.remainingCount > 0, ancestorLines = childAncestorLines)
            if (replies.remainingCount > 0) {
                add(
                    Row.MoreReplies(
                        parentId = id,
                        depth = node.depth + 1,
                        remainingCount = replies.remainingCount,
                        connectors = ThreadConnectors(ancestorLines = childAncestorLines),
                    ),
                )
            }
        }
    }

    private fun branchOf(parentId: Int?): Branch? = if (parentId == null) root else nodes[parentId]?.replies

    private fun withBranch(parentId: Int?, branch: Branch): EntryCommentThreadTree =
        if (parentId == null) {
            copy(root = branch)
        } else {
            copy(nodes = nodes + (parentId to nodes.getValue(parentId).copy(replies = branch)))
        }

    companion object {
        // Server depth limit expressed as our 0-based depth (nesting 7 = depth 5).
        private const val MAX_REPLY_DEPTH = 5

        val empty = EntryCommentThreadTree(
            root = Branch(childIds = emptyList(), totalCount = 0, cursorId = null),
            nodes = emptyMap(),
        )

        fun from(comments: EntryThreadReplies): EntryCommentThreadTree = empty.appendPage(parentId = null, comments)

        private fun MutableMap<Int, Node>.register(comment: EntryThreadComment) {
            put(
                comment.comment.id,
                Node(
                    depth = comment.depth,
                    isByEntryAuthor = comment.isByEntryAuthor,
                    replyParentId = comment.replyParentId,
                    replies = Branch(
                        childIds = comment.replies.items.map { it.comment.id },
                        totalCount = comment.replies.totalCount,
                        cursorId = comment.replies.items.lastOrNull()?.comment?.id,
                    ),
                ),
            )
            comment.replies.items.forEach { register(it) }
        }
    }
}

/** Every comment in [this] page, parents before their replies. */
internal fun EntryThreadReplies.flattenComments(): List<ResourceItem> = buildList {
    fun addAll(replies: EntryThreadReplies) {
        replies.items.forEach { comment ->
            add(comment.comment)
            addAll(comment.replies)
        }
    }
    addAll(this@flattenComments)
}
