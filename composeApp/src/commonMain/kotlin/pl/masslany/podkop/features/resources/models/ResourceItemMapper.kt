package pl.masslany.podkop.features.resources.models

import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

fun ResourceItem.toResourceItemState(isUpcoming: Boolean = false): ResourceItemState =
    when(this.resource) {
        Resource.Entry -> this.toEntryItemState()
        Resource.Link -> this.toLinkItemState(isUpcoming)
        else -> throw UnsupportedOperationException()
    }

private fun ResourceItem.toEntryItemState(): EntryItemState  {
    return EntryItemState(
        id = this.id,
        text = this.content.ifBlank { this.description },
    )
}

private fun ResourceItem.toLinkItemState(isUpcoming: Boolean): LinkItemState {
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
        maxLines = 5
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
        titleState = titleState,
        descriptionState = descriptionState,
        countState = countState,
        authorState = authorState,
        source = source,
        imageUrl = imageUrl,
        tags = tags,
        commentCount = commentCount,
        publishedTimeType = publishedTimeType,
    )
}