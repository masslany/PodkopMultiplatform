package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

fun ResourceItem.toResourceItemState(): ResourceItemState =
    when(this.resource) {
        Resource.Entry -> this.toEntryItemState()
        Resource.Link -> this.toLinkItemState()
        else -> throw UnsupportedOperationException()
    }

private fun ResourceItem.toEntryItemState(): EntryItemState  {
    return EntryItemState(
        text = this.content.ifBlank { this.description },
    )
}

private fun ResourceItem.toLinkItemState(): LinkItemState {
    return LinkItemState(
        text = this.content.ifBlank { this.description },
    )
}