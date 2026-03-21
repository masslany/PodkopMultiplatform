package pl.masslany.podkop.common.models.vote

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Author
import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Comments
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.Media
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Parent
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes

class VoteStateMapperTest {

    @Test
    fun `resource link preserves positive vote highlight from voted state`() {
        val state = resourceItem(
            resource = Resource.Link,
            voted = Voted.Positive,
            actions = actions(voteUp = false, voteDown = true, undoVote = true),
        ).toVoteState()

        val positive = state.positiveVoteButtonState
        assertNotNull(positive)
        assertTrue(positive.isVoted)
    }

    @Test
    fun `resource link preserves negative vote highlight from voted state`() {
        val state = resourceItem(
            resource = Resource.Link,
            voted = Voted.Negative,
            actions = actions(voteUp = true, voteDown = false, undoVote = true),
        ).toVoteState()

        val negative = state.negativeVoteButtonState
        assertNotNull(negative)
        assertTrue(negative.isVoted)
    }

    @Test
    fun `entry resource hides negative vote button even if action says vote_down`() {
        val state = resourceItem(
            resource = Resource.Entry,
            voted = Voted.None,
            actions = actions(voteUp = true, voteDown = true, undoVote = false),
        ).toVoteState()

        assertNull(state.negativeVoteButtonState)
    }

    @Test
    fun `entry comment hides negative vote button even if action says vote_down`() {
        val state = comment(
            resource = Resource.EntryComment,
            voted = Voted.None,
            actions = actions(voteUp = true, voteDown = true, undoVote = false),
        ).toVoteState()

        assertNull(state.negativeVoteButtonState)
    }

    @Test
    fun `link comment preserves positive vote highlight from voted state`() {
        val state = comment(
            resource = Resource.LinkComment,
            voted = Voted.Positive,
            actions = actions(voteUp = false, voteDown = false, undoVote = true),
        ).toVoteState()

        val positive = state.positiveVoteButtonState
        assertNotNull(positive)
        assertTrue(positive.isVoted)
    }

    @Test
    fun `link comment preserves negative vote highlight from voted state`() {
        val state = comment(
            resource = Resource.LinkComment,
            voted = Voted.Negative,
            actions = actions(voteUp = true, voteDown = false, undoVote = true),
        ).toVoteState()

        val negative = state.negativeVoteButtonState
        assertNotNull(negative)
        assertTrue(negative.isVoted)
    }

    @Test
    fun `resource positive button not highlighted when voted none`() {
        val state = resourceItem(
            resource = Resource.Link,
            voted = Voted.None,
            actions = actions(voteUp = true, voteDown = true, undoVote = false),
        ).toVoteState()

        val positive = state.positiveVoteButtonState
        assertNotNull(positive)
        assertFalse(positive.isVoted)
    }
}

private fun resourceItem(
    resource: Resource,
    voted: Voted,
    actions: Actions?,
): ResourceItem = ResourceItem(
    actions = actions,
    adult = false,
    archive = false,
    author = null,
    comments = null,
    content = "",
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = false,
    hot = false,
    id = 1,
    media = null,
    name = "",
    parent = null,
    parentId = null,
    publishedAt = null,
    recommended = false,
    resource = resource,
    slug = "",
    source = null,
    tags = emptyList(),
    title = "",
    voted = voted,
    votes = Votes(count = 0, down = 0, up = 0),
    favourite = false,
)

private fun comment(
    resource: Resource,
    voted: Voted,
    actions: Actions,
): Comment = Comment(
    actions = actions,
    adult = false,
    archive = false,
    author = Author(
        avatar = "",
        blacklist = false,
        color = NameColor.Orange,
        company = false,
        follow = false,
        gender = Gender.Unspecified,
        note = false,
        online = false,
        rank = Rank(position = 0, trend = 0),
        status = "active",
        username = "tester",
        verified = false,
    ),
    blacklist = false,
    comments = null as Comments?,
    content = "",
    createdAt = null,
    deletable = false,
    deleted = Deleted.None,
    device = "",
    editable = false,
    favourite = false,
    id = 1,
    media = Media(embed = null, photo = null, survey = null),
    parentId = Parent(id = 1).id,
    resource = resource,
    slug = "",
    tags = emptyList(),
    voted = voted,
    votes = Votes(count = 0, down = 0, up = 0),
)

private fun actions(
    voteUp: Boolean,
    voteDown: Boolean,
    undoVote: Boolean,
): Actions = Actions(
    create = false,
    createFavourite = false,
    delete = false,
    deleteFavourite = false,
    finishAma = false,
    report = false,
    startAma = false,
    undoVote = undoVote,
    update = false,
    voteDown = voteDown,
    voteUp = voteUp,
    vote = false,
)
