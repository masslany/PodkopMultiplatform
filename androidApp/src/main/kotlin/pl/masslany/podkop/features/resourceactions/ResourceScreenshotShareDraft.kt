package pl.masslany.podkop.features.resourceactions

import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

sealed interface ResourceScreenshotShareDraft {
    data class Entry(val entry: EntryItemState) : ResourceScreenshotShareDraft

    data class EntryComment(val comment: EntryCommentItemState, val parentEntry: EntryItemState? = null) :
        ResourceScreenshotShareDraft

    data class LinkComment(val comment: LinkCommentItemState, val parentComment: LinkCommentItemState? = null) :
        ResourceScreenshotShareDraft
}
