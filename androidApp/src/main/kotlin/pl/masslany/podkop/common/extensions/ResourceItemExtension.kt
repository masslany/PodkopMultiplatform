package pl.masslany.podkop.common.extensions

import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState

fun ResourceItemState.isAuthorReply(parent: ResourceItemState?): Boolean {
    val entryCommentItemState = this as? EntryCommentItemState ?: return false
    val parentEntryItemState = parent as? EntryItemState ?: return false

    val entryAuthor = parentEntryItemState.authorState?.name
    val replyAuthor = entryCommentItemState.authorState?.name

    return entryAuthor == replyAuthor
}
