package pl.masslany.podkop.features.resources.models.link

import pl.masslany.podkop.features.resources.models.ResourceItemState

data class LinkItemState(
    override val id: Int,
    val text: String
) : ResourceItemState
