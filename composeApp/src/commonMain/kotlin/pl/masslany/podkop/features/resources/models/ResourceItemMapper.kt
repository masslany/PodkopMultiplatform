package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.models.entry.toEntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.toEntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.toLinkItemState

fun ResourceItem.toResourceItemState(isUpcoming: Boolean = false): ResourceItemState =
    when (this.resource) {
        Resource.Entry -> this.toEntryItemState()
        Resource.Link -> this.toLinkItemState(isUpcoming)
        Resource.EntryComment -> this.toEntryCommentItemState()
        else -> throw UnsupportedOperationException()
    }
