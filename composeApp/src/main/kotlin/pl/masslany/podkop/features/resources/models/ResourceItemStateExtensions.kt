package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

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

fun ResourceItemState.withFavourite(isFavourite: Boolean): ResourceItemState = when (this) {
    is EntryItemState -> copy(isFavourite = isFavourite)
    is EntryCommentItemState -> copy(isFavourite = isFavourite)
    is LinkItemState -> copy(isFavourite = isFavourite)
    is LinkCommentItemState -> copy(isFavourite = isFavourite)
    else -> this
}

fun ResourceItemState.withEmbedContentState(
    transform: (EmbedContentState?) -> EmbedContentState?,
): ResourceItemState = when (this) {
    is EntryItemState -> copy(embedContentState = transform(embedContentState))
    is EntryCommentItemState -> copy(embedContentState = transform(embedContentState))
    is LinkItemState -> copy(embedContentState = transform(embedContentState))
    is LinkCommentItemState -> copy(embedContentState = transform(embedContentState))
    else -> this
}
