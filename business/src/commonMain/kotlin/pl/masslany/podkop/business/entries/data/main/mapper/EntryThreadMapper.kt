package pl.masslany.podkop.business.entries.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
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
        comments = replies.toEntryThreadReplies(fallbackDepth = 0),
    )

/** [fallbackDepth] is used only when the server leaves out `nesting`. */
internal fun EntryThreadRepliesDto?.toEntryThreadReplies(fallbackDepth: Int): EntryThreadReplies {
    if (this == null) return EntryThreadReplies(totalCount = 0, items = emptyList())
    val comments = items.map { it.toEntryThreadComment(fallbackDepth) }
    return EntryThreadReplies(
        totalCount = maxOf(count, comments.size),
        items = comments,
    )
}

internal fun EntryThreadNodeDto.toEntryThreadComment(fallbackDepth: Int): EntryThreadComment {
    val comment = listOf(item).toResourceItemList(defaultResource = Resource.EntryComment).first()
    val depth = nesting?.let { (it - TOP_LEVEL_COMMENT_NESTING).coerceAtLeast(0) } ?: fallbackDepth
    val parentCommentId = comment.parent?.id?.takeIf { item.parent?.resource == ENTRY_COMMENT_RESOURCE }
    val isAtReplyLimit = nesting != null && nesting >= MAX_REPLY_NESTING

    return EntryThreadComment(
        comment = comment,
        depth = depth,
        isByEntryAuthor = host,
        replyParentId = parentCommentId?.takeIf { isAtReplyLimit } ?: comment.id,
        replies = replies.toEntryThreadReplies(fallbackDepth = depth + 1),
    )
}

private const val ENTRY_COMMENT_RESOURCE = "entry_comment"
