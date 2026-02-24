package pl.masslany.podkop.features.resources.models.entry

import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.embed.toEmbedContentState
import pl.masslany.podkop.common.models.isGifImage
import pl.masslany.podkop.common.models.survey.toSurveyState
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.common.models.vote.toVoteState
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState

internal fun ResourceItem.toEntryItemState(): EntryItemState {
    val author = this.author

    val authorState = author?.let {
        AuthorState(
            name = it.username,
            color = it.color.toNameColorType(),
        )
    }

    val avatarState = if (author != null) {
        AvatarState(
            type = if (author.avatar.isNotEmpty()) {
                AvatarType.NetworkImage(author.avatar)
            } else {
                AvatarType.NoAvatar
            },
            genderIndicatorType = author.gender.toGenderIndicatorType(),
        )
    } else {
        AvatarState(
            type = AvatarType.NoAvatar,
            genderIndicatorType = GenderIndicatorType.Unspecified,
        )
    }

    val entryContentState = when (this.deleted) {
        Deleted.Author -> EntryContentState.DeletedByAuthor
        Deleted.Moderator -> EntryContentState.DeletedByModerator
        Deleted.None -> this.content.toEntryContentState()
    }
    val embedUrl = this.media?.photo?.url
    val embedSource = this.media?.photo?.label
    val embedMimeType = this.media?.photo?.mimeType
    val embedWidth = this.media?.photo?.width
    val embedHeight = this.media?.photo?.height

    val embedImageState = if (embedUrl != null) {
        EmbedImageState(
            url = embedUrl,
            source = embedSource.orEmpty(),
            isAdult = this.adult,
            isGif = isGifImage(
                url = embedUrl,
                mimeType = embedMimeType,
            ),
            width = embedWidth,
            height = embedHeight,
        )
    } else {
        null
    }

    return EntryItemState(
        id = this.id,
        contentType = ResourceType.EntryItem,
        authorState = authorState,
        avatarState = avatarState,
        totalCommentsCount = this.comments?.count ?: 0,
        publishedTimeType = this.createdAt?.toPublishedTimeType(),
        comments = this.comments?.items
            ?.map { it.toEntryCommentItemState() }
            .orEmpty()
            .toImmutableList(),
        voteState = this.toVoteState(),
        entryContentState = entryContentState,
        surveyState = this.media?.survey.toSurveyState(),
        embedImageState = embedImageState,
        embedContentState = this.media?.embed.toEmbedContentState(),
    )
}

private fun Comment.toEntryCommentItemState(): EntryCommentItemState {
    val authorState = AuthorState(
        name = author.username,
        color = author.color.toNameColorType(),
    )

    val avatarState = AvatarState(
        type = if (author.avatar.isNotEmpty()) {
            AvatarType.NetworkImage(author.avatar)
        } else {
            AvatarType.NoAvatar
        },
        genderIndicatorType = author.gender.toGenderIndicatorType(),
    )

    val entryContentState = when (this.deleted) {
        Deleted.Author -> EntryContentState.DeletedByAuthor
        Deleted.Moderator -> EntryContentState.DeletedByModerator
        Deleted.None -> this.content.toEntryContentState()
    }

    val embedUrl = this.media.photo?.url
    val embedSource = this.media.photo?.label
    val embedMimeType = this.media.photo?.mimeType
    val embedWidth = this.media.photo?.width
    val embedHeight = this.media.photo?.height

    val embedImageState = if (embedUrl != null) {
        EmbedImageState(
            url = embedUrl,
            source = embedSource.orEmpty(),
            isAdult = this.adult,
            isGif = isGifImage(
                url = embedUrl,
                mimeType = embedMimeType,
            ),
            width = embedWidth,
            height = embedHeight,
        )
    } else {
        null
    }

    return EntryCommentItemState(
        id = this.id,
        contentType = ResourceType.EntryCommentItem,
        parentId = this.parentId,
        authorState = authorState,
        avatarState = avatarState,
        publishedTimeType = this.createdAt?.toPublishedTimeType(),
        voteState = this.toVoteState(),
        entryContentState = entryContentState,
        embedImageState = embedImageState,
        embedContentState = this.media.embed.toEmbedContentState(),
    )
}
