package pl.masslany.podkop.features.resources.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.models.survey.AnswerState
import pl.masslany.podkop.common.models.survey.SurveyState
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.common.models.vote.VoteValueType
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

class LinkCommentItemStateProvider : PreviewParameterProvider<LinkCommentItemState> {
    override val values: Sequence<LinkCommentItemState> = sequenceOf(
        linkCommentBase(
            id = 100,
            linkId = 42,
            content = "Looks great — previews finally cover the real states.\n\n#compose @patryk",
            vote = PreviewFixtures.voteState(),
            embedImageState = null,
            replies = persistentListOf(),
        ),
        linkCommentBase(
            id = 101,
            linkId = 42,
            content = "I disagree. The API surface should stay minimal.",
            vote = PreviewFixtures.voteState(value = VoteValueType.Negative("-2")),
            embedImageState = EmbedImageState(
                url = "https://picsum.photos/seed/comment/600/400",
                source = PreviewFixtures.DOMAIN,
                isAdult = false,
                isGif = false,
                width = 600,
                height = 400,
            ),
            replies = persistentListOf(
                linkCommentBase(
                    id = 102,
                    linkId = 42,
                    parentId = 101,
                    content = "Counterpoint: previews are part of DX and help keep UI stable.",
                    vote = PreviewFixtures.voteState(value = VoteValueType.Positive("5")),
                ),
            ),
        ),
    )
}

class EntryCommentItemStateProvider : PreviewParameterProvider<EntryCommentItemState> {
    override val values: Sequence<EntryCommentItemState> = sequenceOf(
        entryCommentBase(
            id = 200,
            parentId = 77,
            content = "Nice write-up. Can you share the benchmark setup?",
            publishedTimeType = PublishedTimeType.Minutes(18),
        ),
        entryCommentBase(
            id = 201,
            parentId = 77,
            content = "This was deleted by moderator in prod — check how it looks.",
            entryContentState = EntryContentState.DeletedByModerator,
            publishedTimeType = PublishedTimeType.Days(1),
        ),
    )
}

class LinkItemStateProvider : PreviewParameterProvider<LinkItemState> {
    override val values: Sequence<LinkItemState> = sequenceOf(
        LinkItemState(
            id = 42,
            contentType = ResourceType.LinkItem,
            slug = "slug-preview-42",
            titleState = PreviewFixtures.titleState(maxLines = 3),
            descriptionState = PreviewFixtures.descriptionState(
                description = PreviewFixtures.LONG_BODY,
                maxLines = 3,
            ),
            countState = PreviewFixtures.countState(count = "512", isHot = true),
            authorState = PreviewFixtures.authorState(),
            source = PreviewFixtures.DOMAIN,
            sourceUrl = PreviewFixtures.URL,
            publishedTimeType = PreviewFixtures.publishedType(),
            commentCount = 12,
            imageUrl = "https://picsum.photos/seed/link/400/400",
            tags = PreviewFixtures.tags3(),
            comments = persistentListOf(),
            embedContentState = null,
        ),
        LinkItemState(
            id = 43,
            contentType = ResourceType.LinkItem,
            slug = "slug-preview-43",
            titleState = PreviewFixtures.titleState(title = PreviewFixtures.LONG_TITLE, maxLines = 2),
            descriptionState = null,
            countState = PreviewFixtures.countState(count = "12"),
            authorState = PreviewFixtures.authorState(name = PreviewFixtures.USERNAME_ALT),
            source = "github.com",
            sourceUrl = "https://github.com",
            publishedTimeType = PublishedTimeType.Days(2),
            commentCount = 98,
            imageUrl = "",
            tags = PreviewFixtures.tags3(),
            comments = persistentListOf(
                linkCommentBase(
                    id = 103,
                    linkId = 43,
                    content = "The embed preview should show loading states as well.",
                    vote = PreviewFixtures.voteState(value = VoteValueType.Positive("9")),
                ),
            ),
            embedContentState = PreviewFixtures.embedStateTwitterLoaded(),
        ),
    )
}

class EntryItemStateProvider : PreviewParameterProvider<EntryItemState> {
    override val values: Sequence<EntryItemState> = sequenceOf(
        EntryItemState(
            id = 77,
            contentType = ResourceType.EntryItem,
            avatarState = avatarNetwork(),
            authorState = PreviewFixtures.authorState(),
            totalCommentsCount = 24,
            comments = persistentListOf(
                entryCommentBase(id = 202, parentId = 77, content = "First!"),
                entryCommentBase(id = 203, parentId = 77, content = "Great post, thanks."),
            ),
            entryContentState = PreviewFixtures.LONG_BODY.toEntryContentState(isDownVoted = false),
            publishedTimeType = PublishedTimeType.Hours(6),
            voteState = PreviewFixtures.voteState(
                value = VoteValueType.Positive("31"),
                upVoted = true,
            ),
            surveyState = null,
            embedImageState = EmbedImageState(
                url = "https://picsum.photos/seed/entry/800/600",
                source = PreviewFixtures.DOMAIN,
                isAdult = false,
                isGif = false,
                width = 800,
                height = 600,
            ),
            embedContentState = PreviewFixtures.embedStateTwitterLoading(),
        ),
        EntryItemState(
            id = 78,
            contentType = ResourceType.EntryItem,
            avatarState = avatarNoAvatar(),
            authorState = PreviewFixtures.authorState(name = "anonymous"),
            totalCommentsCount = 0,
            comments = persistentListOf(),
            entryContentState = "Short note with #tag and @mention".toEntryContentState(isDownVoted = false),
            publishedTimeType = PublishedTimeType.Now,
            voteState = PreviewFixtures.voteState(value = VoteValueType.Zero),
            surveyState = SurveyState(
                question = "Which preview states do you value most?",
                answers = persistentListOf(
                    AnswerState(
                        isSelected = true,
                        text = "Loading / error / empty",
                        count = 128,
                        percentageFraction = 0.64f,
                        percentage = "64%",
                    ),
                    AnswerState(
                        isSelected = false,
                        text = "Typography & spacing",
                        count = 52,
                        percentageFraction = 0.26f,
                        percentage = "26%",
                    ),
                    AnswerState(
                        isSelected = false,
                        text = "Dark mode",
                        count = 20,
                        percentageFraction = 0.10f,
                        percentage = "10%",
                    ),
                ),
                count = 200,
            ),
            embedImageState = null,
            embedContentState = null,
        ),
    )
}

class ResourceItemStateProvider : PreviewParameterProvider<ResourceItemState> {
    override val values: Sequence<ResourceItemState> = sequenceOf(
        LinkItemStateProvider().values.first(),
        EntryItemStateProvider().values.first(),
        LinkCommentItemStateProvider().values.first(),
        EntryCommentItemStateProvider().values.first(),
        HitItemState(
            id = 9,
            contentType = ResourceType.HitItem,
            titleState = PreviewFixtures.titleState(title = "Hit of the day", maxLines = 2),
            countState = PreviewFixtures.countState(count = "2048", isHot = true),
            imageUrl = "",
            isAdult = false,
        ),
    )
}

private fun avatarNetwork(): AvatarState = AvatarState(
    type = AvatarType.NetworkImage(url = "https://picsum.photos/seed/avatar/96/96"),
    genderIndicatorType = GenderIndicatorType.Male,
)

private fun avatarNoAvatar(): AvatarState = AvatarState(
    type = AvatarType.NoAvatar,
    genderIndicatorType = GenderIndicatorType.Unspecified,
)

private fun linkCommentBase(
    id: Int,
    linkId: Int,
    parentId: Int = 0,
    content: String,
    vote: VoteState,
    publishedTimeType: PublishedTimeType? = PublishedTimeType.HoursMinutes(1, 4),
    embedImageState: EmbedImageState? = null,
    replies: ImmutableList<LinkCommentItemState> = persistentListOf(),
) = LinkCommentItemState(
    id = id,
    contentType = ResourceType.LinkCommentItem,
    linkId = linkId,
    linkSlug = "example-link-slug",
    parentId = parentId,
    avatarState = avatarNetwork(),
    authorState = PreviewFixtures.authorState(),
    entryContentState = content.toEntryContentState(isDownVoted = false),
    publishedTimeType = publishedTimeType,
    voteState = vote,
    embedImageState = embedImageState,
    replies = replies,
    embedContentState = null,
)

private fun entryCommentBase(
    id: Int,
    parentId: Int,
    content: String,
    entryContentState: EntryContentState = content.toEntryContentState(isDownVoted = false),
    publishedTimeType: PublishedTimeType? = PublishedTimeType.Minutes(8),
) = EntryCommentItemState(
    id = id,
    contentType = ResourceType.EntryCommentItem,
    parentId = parentId,
    avatarState = avatarNoAvatar(),
    authorState = PreviewFixtures.authorState(),
    entryContentState = entryContentState,
    publishedTimeType = publishedTimeType,
    voteState = PreviewFixtures.voteState(value = VoteValueType.Positive("3")),
    embedImageState = null,
    embedContentState = null,
)
