package pl.masslany.podkop.features.resources.models.comment

import pl.masslany.podkop.features.resources.models.ResourceItemState

data class CommentItemState(
    override val id: Int,
    val text: String
) : ResourceItemState
