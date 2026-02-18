package pl.masslany.podkop.features.resources.models.related

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.vote.toVoteState
import pl.masslany.podkop.features.resources.models.ResourceType

internal fun ResourceItem.toRelatedItemState(): RelatedItemState {
    val titleState = this.title.ifBlank { null }?.let {
        TitleState(
            title = this.title,
            maxLines = 2,
            isAdult = this.adult,
            displayAdultBadge = true,
        )
    }

    val authorState = this.author?.let {
        AuthorState(
            name = it.username,
            color = it.color.toNameColorType(),
        )
    }

    return RelatedItemState(
        id = this.id,
        contentType = ResourceType.RelatedItem,
        imageUrl = this.media?.photo?.url.orEmpty(),
        titleState = titleState,
        authorState = authorState,
        source = this.source?.label,
        sourceUrl = this.source?.url.orEmpty(),
        voteState = this.votes?.let { this.toVoteState() },
    )
}
