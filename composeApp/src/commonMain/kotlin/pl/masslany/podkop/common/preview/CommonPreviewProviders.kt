package pl.masslany.podkop.common.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.common.models.vote.VoteValueType

class CountStateProvider : PreviewParameterProvider<CountState> {
    override val values: Sequence<CountState> = sequenceOf(
        PreviewFixtures.countState(count = "1", isHot = false, isVoted = false, canVote = true),
        PreviewFixtures.countState(count = "128", isHot = true, isVoted = false, canVote = true),
        PreviewFixtures.countState(count = "42", isHot = false, isVoted = true, canVote = true),
        PreviewFixtures.countState(count = "0", isHot = false, isVoted = false, canVote = false),
    )
}

class TitleStateProvider : PreviewParameterProvider<TitleState> {
    override val values: Sequence<TitleState> = sequenceOf(
        PreviewFixtures.titleState(title = PreviewFixtures.SHORT_TITLE, maxLines = 2),
        PreviewFixtures.titleState(title = PreviewFixtures.LONG_TITLE, maxLines = 3),
        PreviewFixtures.titleState(title = "NSFW content", isAdult = true, displayAdultBadge = true, maxLines = 1),
    )
}

class AuthorStateProvider : PreviewParameterProvider<AuthorState> {
    override val values: Sequence<AuthorState> = sequenceOf(
        PreviewFixtures.authorState(name = PreviewFixtures.USERNAME, color = NameColorType.Orange),
        PreviewFixtures.authorState(name = PreviewFixtures.USERNAME_ALT, color = NameColorType.Green),
        PreviewFixtures.authorState(name = "moderator", color = NameColorType.Burgundy),
    )
}

class DescriptionStateProvider : PreviewParameterProvider<DescriptionState> {
    override val values: Sequence<DescriptionState> = sequenceOf(
        PreviewFixtures.descriptionState(description = PreviewFixtures.SHORT_BODY, maxLines = 2),
        PreviewFixtures.descriptionState(description = PreviewFixtures.LONG_BODY, maxLines = 3),
        PreviewFixtures.descriptionState(description = PreviewFixtures.LONG_BODY, maxLines = 6),
    )
}

class TagItemProvider : PreviewParameterProvider<TagItem> {
    override val values: Sequence<TagItem> = sequenceOf(
        TagItem("#compose", needsSpacer = true),
        TagItem("#kotlin", needsSpacer = true),
        TagItem("#multiplatform", needsSpacer = false),
    )
}

class TagListProvider : PreviewParameterProvider<ImmutableList<TagItem>> {
    override val values: Sequence<ImmutableList<TagItem>> = sequenceOf(
        PreviewFixtures.tags3(),
        persistentListOf(TagItem("#single", needsSpacer = false)),
        persistentListOf(
            TagItem("#very", needsSpacer = true),
            TagItem("#long", needsSpacer = true),
            TagItem("#tag-list", needsSpacer = true),
            TagItem("#for", needsSpacer = true),
            TagItem("#wrapping", needsSpacer = false),
        ),
    )
}

class PublishedTimeTypeProvider : PreviewParameterProvider<PublishedTimeType> {
    override val values: Sequence<PublishedTimeType> = sequenceOf(
        PublishedTimeType.Now,
        PublishedTimeType.Minutes(8),
        PublishedTimeType.HoursMinutes(hours = 3, minutes = 12),
        PublishedTimeType.Days(2),
        PublishedTimeType.FullDate("2026.02.21 14:05"),
    )
}

class EmbedContentStateProvider : PreviewParameterProvider<EmbedContentState> {
    override val values: Sequence<EmbedContentState> = sequenceOf(
        PreviewFixtures.embedStateTwitterLoading(),
        PreviewFixtures.embedStateTwitterLoaded(),
        EmbedContentState(
            key = "yt-1",
            type = EmbedContentType.Youtube,
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            thumbnailUrl = "https://picsum.photos/seed/youtube/640/360",
            twitterState = null,
        ),
        EmbedContentState(
            key = "tw-err",
            type = EmbedContentType.Twitter,
            url = "https://x.com/compose/status/404",
            thumbnailUrl = "",
            twitterState = TwitterEmbedState.Error,
        ),
    )
}

class OptionalEmbedContentStateProvider : PreviewParameterProvider<EmbedContentState?> {
    override val values: Sequence<EmbedContentState?> = sequenceOf(
        null,
        PreviewFixtures.embedStateTwitterLoading(),
        PreviewFixtures.embedStateTwitterLoaded(),
    )
}

class VoteStateProvider : PreviewParameterProvider<VoteState> {
    override val values: Sequence<VoteState> = sequenceOf(
        PreviewFixtures.voteState(value = VoteValueType.Zero),
        PreviewFixtures.voteState(value = VoteValueType.Positive("128"), upVoted = true),
        PreviewFixtures.voteState(value = VoteValueType.Negative("-12"), downVoted = true),
    )
}
