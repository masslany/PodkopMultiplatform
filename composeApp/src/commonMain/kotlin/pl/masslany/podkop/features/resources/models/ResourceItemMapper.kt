package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.models.entry.toEntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.toEntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.toLinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.toLinkCommentItemState

fun ResourceItem.toResourceItemState(isUpcoming: Boolean = false): ResourceItemState =
    when (this.resource) {
        Resource.Entry -> this.toEntryItemState()
        Resource.Link -> this.toLinkItemState(isUpcoming)
        Resource.EntryComment -> this.toEntryCommentItemState()
        Resource.LinkComment -> this.toLinkCommentItemState()
        else -> throw UnsupportedOperationException("Unsupported resource type ${this.resource}")
    }
