package pl.masslany.podkop.features.linkdetails

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.models.vote.VoteButtonState
import pl.masslany.podkop.common.models.vote.VoteButtonType
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.common.models.vote.VoteValueType
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

class LinkDetailsViewModelTest {

    @Test
    fun `applyTwitterEmbedStateById updates matching reply embed state`() {
        val reply = linkCommentState(
            id = 200,
            parentId = 100,
            embedContentState = twitterEmbedState(key = "reply-embed"),
        )
        val comments = persistentListOf(
            linkCommentState(
                id = 100,
                parentId = 42,
                replies = persistentListOf(reply),
            ),
        )

        val updated = comments.applyTwitterEmbedStateById(
            commentId = 200,
            embedKey = "reply-embed",
            newState = TwitterEmbedState.Loading,
        )

        val updatedReplyState = updated.single().replies.single().embedContentState?.twitterState
        assertIs<TwitterEmbedState.Loading>(updatedReplyState)
    }

    @Test
    fun `applyTwitterEmbedStateById ignores replies when embed key does not match`() {
        val reply = linkCommentState(
            id = 200,
            parentId = 100,
            embedContentState = twitterEmbedState(key = "reply-embed"),
        )
        val comments = persistentListOf(
            linkCommentState(
                id = 100,
                parentId = 42,
                replies = persistentListOf(reply),
            ),
        )

        val updated = comments.applyTwitterEmbedStateById(
            commentId = 200,
            embedKey = "other-embed",
            newState = TwitterEmbedState.Error,
        )

        assertEquals(
            TwitterEmbedState.Preview,
            updated.single().replies.single().embedContentState?.twitterState,
        )
    }
}

private fun linkCommentState(
    id: Int,
    parentId: Int,
    replies: ImmutableList<LinkCommentItemState> = persistentListOf(),
    embedContentState: EmbedContentState? = null,
) = LinkCommentItemState(
    id = id,
    contentType = ResourceType.LinkCommentItem,
    linkId = 42,
    linkSlug = "example-link",
    parentId = parentId,
    avatarState = AvatarState(
        type = AvatarType.NoAvatar,
        genderIndicatorType = GenderIndicatorType.Unspecified,
    ),
    authorState = AuthorState(
        name = "patryk",
        color = NameColorType.Orange,
    ),
    entryContentState = "hello".toEntryContentState(isDownVoted = false),
    publishedTimeType = PublishedTimeType.Minutes(5),
    voteState = VoteState(
        voteValueType = VoteValueType.Positive("5"),
        positiveVoteButtonState = VoteButtonState(
            voteButtonType = VoteButtonType.Positive,
            isVoted = false,
        ),
        negativeVoteButtonState = VoteButtonState(
            voteButtonType = VoteButtonType.Negative,
            isVoted = false,
        ),
    ),
    embedImageState = null,
    replies = replies,
    embedContentState = embedContentState,
)

private fun twitterEmbedState(key: String) = EmbedContentState(
    key = key,
    type = EmbedContentType.Twitter,
    url = "https://x.com/podkop/status/123",
    thumbnailUrl = "https://example.com/thumb.jpg",
    twitterState = TwitterEmbedState.Preview,
)
