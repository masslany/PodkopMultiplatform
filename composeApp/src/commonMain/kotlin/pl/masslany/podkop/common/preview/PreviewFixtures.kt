package pl.masslany.podkop.common.preview

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.TwitterEmbedPreviewState
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.vote.VoteButtonState
import pl.masslany.podkop.common.models.vote.VoteButtonType
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.common.models.vote.VoteValueType

object PreviewFixtures {

    const val USERNAME = "patryk"
    const val USERNAME_ALT = "maria_dev"
    const val DOMAIN = "podkop.app"
    const val URL = "https://podkop.app"

    const val SHORT_TITLE = "Compose Multiplatform: practical patterns for production apps"
    const val LONG_TITLE =
        "Compose Multiplatform: practical patterns for production apps " +
            "(DI, navigation, previews, and performance tuning)"

    const val SHORT_BODY = "A short description that looks like real content."
    const val LONG_BODY =
        "Compose Multiplatform lets you share UI across Android and iOS. " +
            "This preview uses longer text to validate truncation, spacing, and typography. " +
            "It should feel like content you’d see in production."

    fun countState(
        count: String = "128",
        isHot: Boolean = false,
        isVoted: Boolean = false,
        canVote: Boolean = true,
    ) = CountState(
        count = count,
        isHot = isHot,
        isVoted = isVoted,
        canVote = canVote,
    )

    fun titleState(
        title: String = SHORT_TITLE,
        maxLines: Int = 3,
        isAdult: Boolean = false,
        displayAdultBadge: Boolean = false,
    ) = TitleState(
        title = title,
        maxLines = maxLines,
        isAdult = isAdult,
        displayAdultBadge = displayAdultBadge,
    )

    fun authorState(
        name: String = USERNAME,
        color: NameColorType = NameColorType.Orange,
    ) = AuthorState(name = name, color = color)

    fun descriptionState(
        description: String = SHORT_BODY,
        maxLines: Int = 3,
    ) = DescriptionState(description = description, maxLines = maxLines)

    fun tags3(): ImmutableList<TagItem> = persistentListOf(
        TagItem("compose", needsSpacer = true),
        TagItem("kotlin", needsSpacer = true),
        TagItem("multiplatform", needsSpacer = false),
    )

    fun embedStateTwitterLoading(
        url: String = "https://x.com/compose/status/123",
    ) = EmbedContentState(
        key = "twitter-1",
        type = EmbedContentType.Twitter,
        url = url,
        thumbnailUrl = "",
        twitterState = TwitterEmbedState.Loading,
    )

    fun embedStateTwitterLoaded(
        url: String = "https://x.com/compose/status/123",
    ) = EmbedContentState(
        key = "twitter-2",
        type = EmbedContentType.Twitter,
        url = url,
        thumbnailUrl = "https://picsum.photos/seed/compose/400/200",
        twitterState = TwitterEmbedState.Loaded(
            tweet = TwitterEmbedPreviewState(
                authorName = "JetBrains Compose",
                authorHandle = "@compose",
                avatarUrl = "https://picsum.photos/seed/avatar/96/96",
                text = "Previews should cover realistic states: loading, error, and content.",
                replyCount = 12,
                retweetCount = 34,
                likeCount = 128,
                mediaThumbnailUrl = "https://picsum.photos/seed/media/640/360",
                mediaAspectRatio = 16f / 9f,
            ),
        ),
    )

    fun publishedType(): PublishedTimeType = PublishedTimeType.HoursMinutes(hours = 3, minutes = 12)

    fun voteState(
        value: VoteValueType = VoteValueType.Positive("42"),
        upVoted: Boolean = false,
        downVoted: Boolean = false,
    ) = VoteState(
        voteValueType = value,
        positiveVoteButtonState = VoteButtonState(
            voteButtonType = VoteButtonType.Positive,
            isVoted = upVoted,
        ),
        negativeVoteButtonState = VoteButtonState(
            voteButtonType = VoteButtonType.Negative,
            isVoted = downVoted,
        ),
    )
}
