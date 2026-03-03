package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState

fun ResourceItemState.toDeletedByAuthor(): ResourceItemState = when (this) {
    is EntryItemState -> copy(
        entryContentState = EntryContentState.DeletedByAuthor,
        isDeleteEnabled = false,
    )

    is EntryCommentItemState -> copy(
        entryContentState = EntryContentState.DeletedByAuthor,
        isDeleteEnabled = false,
    )

    else -> this
}
