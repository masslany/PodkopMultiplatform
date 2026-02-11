package pl.masslany.podkop.features.resources.models.link

import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.features.resources.models.ResourceType

internal fun ResourceItem.toLinkItemState(isUpcoming: Boolean): LinkItemState {
    val titleState = this.title.ifBlank { null }?.let {
        TitleState(
            title = this.title,
            maxLines = Int.MAX_VALUE,
            isAdult = this.adult,
            displayAdultBadge = true,
        )
    }

    val descriptionState = DescriptionState(
        description = this.description,
        maxLines = 5,
    )

    val votes = this.votes
    val countState = if (votes != null) {
        val canVote = this.actions?.voteUp ?: false ||
            this.actions?.voteDown ?: false

        CountState(
            count = votes.up.toString(),
            isHot = this.hot,
            isVoted = this.voted,
            canVote = canVote,
        )
    } else {
        CountState(
            count = "-",
            isHot = false,
            isVoted = false,
            canVote = false,
        )
    }

    val authorState = this.author?.let {
        AuthorState(
            name = it.username,
            color = it.color.toNameColorType(),
        )
    }

    val source = this.source?.label
    val sourceUrl = this.source?.url

    val imageUrl = this.media?.photo?.url.orEmpty()

    val tags = this.tags.mapIndexed { index, tag ->
        TagItem(
            tag = tag,
            needsSpacer = index != this.tags.lastIndex,
        )
    }.toImmutableList()

    val commentCount = this.comments?.count ?: 0

    val publishedTimeType = if (isUpcoming) {
        this.createdAt?.toPublishedTimeType()
    } else {
        this.publishedAt?.toPublishedTimeType()
    }

    return LinkItemState(
        id = this.id,
        contentType = ResourceType.LinkItem,
        titleState = titleState,
        descriptionState = descriptionState,
        countState = countState,
        authorState = authorState,
        source = source,
        sourceUrl = sourceUrl.orEmpty(),
        imageUrl = imageUrl,
        tags = tags,
        commentCount = commentCount,
        publishedTimeType = publishedTimeType,
    )
}
