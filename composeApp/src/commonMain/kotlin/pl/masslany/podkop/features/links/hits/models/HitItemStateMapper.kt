package pl.masslany.podkop.features.links.hits.models

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.TitleState

fun ResourceItem.toHitItemState(): HitItemState {
    val canVote = this.actions?.voteUp ?: false ||
        this.actions?.voteDown ?: false

    val imageUrl = this.media?.photo?.url.orEmpty()

    return HitItemState(
        titleState = TitleState(
            title = this.title,
            maxLines = 3,
            isAdult = this.adult,
            displayAdultBadge = false,
        ),
        id = this.id,
        countState = CountState(
            count = (votes?.up ?: 0).toString(),
            isHot = this.hot,
            isVoted = false,
            canVote = canVote,
        ),
        imageUrl = imageUrl,
        isAdult = this.adult,
    )
}
