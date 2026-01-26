package pl.masslany.podkop.features.resources.models.entry

import pl.masslany.podkop.features.resources.models.ResourceItemState

data class EntryItemState(
    override val id: Int,
    val text: String
) : ResourceItemState