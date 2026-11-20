package pl.masslany.podkop.features.resources.models.related

import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType

data class RelatedItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val imageUrl: String,
    val titleState: TitleState?,
    val authorState: AuthorState?,
    val source: String?,
    val sourceUrl: String,
    val voteState: VoteState?,
) : ResourceItemState
