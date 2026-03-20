package pl.masslany.podkop.features.resources.models.entrycomment

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
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.common.models.vote.toVoteState
import pl.masslany.podkop.features.resources.models.ResourceType

internal fun ResourceItem.toEntryCommentItemState(): EntryCommentItemState {
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
        Deleted.Host -> EntryContentState.DeletedByEntryAuthor
        Deleted.Moderator -> EntryContentState.DeletedByModerator
        Deleted.None -> this.content.toEntryContentState(isDownVoted = false) // Entry cannot be downvoted
    }

    val embedUrl = this.media?.photo?.url
    val embedSource = this.media?.photo?.label
    val embedMimeType = this.media?.photo?.mimeType
    val embedWidth = this.media?.photo?.width
    val embedHeight = this.media?.photo?.height
    val embedKey = this.media?.photo?.key

    val embedImageState = if (embedUrl != null) {
        EmbedImageState(
            url = embedUrl,
            key = embedKey,
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
        parentId = this.parent?.id ?: 0,
        authorState = authorState,
        avatarState = avatarState,
        isBlacklisted = author?.blacklist == true,
        publishedTimeType = this.createdAt?.toPublishedTimeType(),
        voteState = this.toVoteState(),
        isReplyEnabled = this.actions?.create == true,
        isFavourite = this.favourite,
        isFavouriteEnabled = this.actions?.let { it.createFavourite || it.deleteFavourite } ?: false,
        isDeleteEnabled = this.actions?.delete == true || this.deletable,
        isEditEnabled = this.actions?.update == true || this.editable,
        rawContent = this.content,
        adult = this.adult,
        entryContentState = entryContentState,
        embedImageState = embedImageState,
        embedContentState = this.media?.embed.toEmbedContentState(),
    )
}
