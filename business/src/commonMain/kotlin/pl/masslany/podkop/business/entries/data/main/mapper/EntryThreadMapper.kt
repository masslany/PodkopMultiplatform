package pl.masslany.podkop.business.entries.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.domain.models.common.Parent
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadNodeDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadRepliesDto
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies

// Server nesting counts the entry as 1, so its direct comments are 2.
private const val TOP_LEVEL_COMMENT_NESTING = 2

// Deepest nesting the service accepts replies under (`maxLevelNesting` in its web client).
internal const val MAX_REPLY_NESTING = 7

internal fun EntryThreadNodeDto.toEntryThread(): EntryThread =
    EntryThread(
        entry = listOf(item).toResourceItemList(defaultResource = Resource.Entry).first(),
        comments = replies.toEntryThreadReplies(entryId = item.id ?: -1, fallbackDepth = 0),
    )

/** [fallbackDepth] is used only when the server leaves out `nesting`. */
internal fun EntryThreadRepliesDto?.toEntryThreadReplies(entryId: Int, fallbackDepth: Int): EntryThreadReplies {
    if (this == null) return EntryThreadReplies(totalCount = 0, items = emptyList())
    val comments = items.map { it.toEntryThreadComment(entryId, fallbackDepth) }
    return EntryThreadReplies(
        totalCount = maxOf(count, comments.size),
        items = comments,
    )
}

/**
 * Nested replies name their parent comment as `parent`, but every entry comment action (votes,
 * edits, deletes) addresses the comment under its entry, so [entryId] replaces it. The reply
 * structure lives in the thread itself.
 */
internal fun EntryThreadNodeDto.toEntryThreadComment(entryId: Int, fallbackDepth: Int): EntryThreadComment {
    val mapped = listOf(item).toResourceItemList(defaultResource = Resource.EntryComment).first()
    val comment = mapped.copy(parent = Parent(id = entryId), parentId = entryId)
    val depth = nesting?.let { (it - TOP_LEVEL_COMMENT_NESTING).coerceAtLeast(0) } ?: fallbackDepth
    val parentCommentId = item.parent?.id?.takeIf { item.parent.resource == ENTRY_COMMENT_RESOURCE }
    val isAtReplyLimit = nesting != null && nesting >= MAX_REPLY_NESTING

    return EntryThreadComment(
        comment = comment,
        depth = depth,
        isByEntryAuthor = host,
        replyParentId = parentCommentId?.takeIf { isAtReplyLimit } ?: comment.id,
        replies = replies.toEntryThreadReplies(entryId = entryId, fallbackDepth = depth + 1),
    )
}

private const val ENTRY_COMMENT_RESOURCE = "entry_comment"
