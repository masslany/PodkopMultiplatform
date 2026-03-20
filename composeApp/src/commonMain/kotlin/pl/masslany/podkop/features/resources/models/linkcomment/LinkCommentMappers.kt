package pl.masslany.podkop.features.resources.models.linkcomment

import kotlinx.collections.immutable.persistentListOf
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
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.common.models.vote.VoteValueType
import pl.masslany.podkop.common.models.vote.toVoteState
import pl.masslany.podkop.features.resources.models.ResourceType

internal fun ResourceItem.toLinkCommentItemState(
    linkIdOverride: Int? = null,
    linkSlugOverride: String? = null,
): LinkCommentItemState {
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

    val voteState = this.toVoteState()
    val isDownVoted = voteState.voteValueType is VoteValueType.Negative

    val entryContentState = when (this.deleted) {
        Deleted.Author -> EntryContentState.DeletedByAuthor
        Deleted.Host -> EntryContentState.DeletedByEntryAuthor
        Deleted.Moderator -> EntryContentState.DeletedByModerator
        Deleted.None -> this.content.toEntryContentState(isDownVoted = isDownVoted)
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

    val resolvedLinkId = linkIdOverride ?: this.parent?.linkId ?: this.parent?.id ?: 0
    val resolvedLinkSlug = linkSlugOverride ?: this.slug
    val resolvedParentId = this.parentId ?: -1
    val replies = this.comments
        ?.items
        .orEmpty()
        .map { it.toLinkCommentItemState(linkId = resolvedLinkId, linkSlug = resolvedLinkSlug) }
        .toImmutableList()

    return LinkCommentItemState(
        id = this.id,
        contentType = ResourceType.LinkCommentItem,
        linkId = resolvedLinkId,
        linkSlug = resolvedLinkSlug,
        parentId = resolvedParentId,
        avatarState = avatarState,
        authorState = authorState,
        isBlacklisted = author?.blacklist == true,
        entryContentState = entryContentState,
        publishedTimeType = this.createdAt?.toPublishedTimeType(),
        voteState = voteState,
        isReplyEnabled = this.actions?.create == true,
        isFavourite = this.favourite,
        isFavouriteEnabled = this.actions?.let { it.createFavourite || it.deleteFavourite } ?: false,
        isEditEnabled = this.actions?.update == true || this.editable,
        rawContent = this.content,
        adult = this.adult,
        embedImageState = embedImageState,
        replies = replies,
        embedContentState = this.media?.embed.toEmbedContentState(),
    )
}

internal fun Comment.toLinkCommentItemState(
    linkId: Int,
    linkSlug: String,
): LinkCommentItemState {
    val authorState = AuthorState(
        name = this.author.username,
        color = this.author.color.toNameColorType(),
    )

    val avatarState = AvatarState(
        type = if (this.author.avatar.isNotEmpty()) {
            AvatarType.NetworkImage(this.author.avatar)
        } else {
            AvatarType.NoAvatar
        },
        genderIndicatorType = this.author.gender.toGenderIndicatorType(),
    )

    val voteState = this.toVoteState()
    val isDownVoted = voteState.voteValueType is VoteValueType.Negative

    val entryContentState = when (this.deleted) {
        Deleted.Author -> EntryContentState.DeletedByAuthor
        Deleted.Host -> EntryContentState.DeletedByAuthor
        Deleted.Moderator -> EntryContentState.DeletedByModerator
        Deleted.None -> this.content.toEntryContentState(isDownVoted = isDownVoted)
    }

    val embedUrl = this.media.photo?.url
    val embedSource = this.media.photo?.label
    val embedMimeType = this.media.photo?.mimeType
    val embedWidth = this.media.photo?.width
    val embedHeight = this.media.photo?.height
    val embedKey = this.media.embed?.key

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

    return LinkCommentItemState(
        id = this.id,
        contentType = ResourceType.LinkCommentItem,
        linkId = linkId,
        linkSlug = linkSlug,
        parentId = this.parentId,
        avatarState = avatarState,
        authorState = authorState,
        isBlacklisted = this.blacklist || this.author.blacklist,
        entryContentState = entryContentState,
        publishedTimeType = this.createdAt?.toPublishedTimeType(),
        voteState = voteState,
        isReplyEnabled = this.actions.create,
        isFavourite = this.favourite,
        isFavouriteEnabled = this.actions.createFavourite || this.actions.deleteFavourite,
        isEditEnabled = this.actions.update || this.editable,
        rawContent = this.content,
        adult = this.adult,
        embedImageState = embedImageState,
        replies = persistentListOf(),
        embedContentState = this.media.embed.toEmbedContentState(),
    )
}
