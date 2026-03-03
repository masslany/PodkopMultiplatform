package pl.masslany.podkop.features.resources.models.entrycomment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Author
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
import pl.masslany.podkop.common.models.EntryContentState

class EntryCommentMappersTest {

    @Test
    fun `entry comment mapper keeps content not downvoted even when voted negative`() {
        val state = entryCommentResource(voted = Voted.Negative).toEntryCommentItemState()

        val contentState = assertIs<EntryContentState.Content>(state.entryContentState)
        assertEquals(false, contentState.isDownVoted)
    }
}

private fun entryCommentResource(
    voted: Voted = Voted.None,
): ResourceItem = ResourceItem(
    actions = actions(),
    adult = false,
    archive = false,
    author = author(),
    comments = null,
    content = "entry comment",
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = false,
    hot = false,
    id = 1,
    media = media(),
    name = "",
    parent = Parent(id = 999),
    parentId = 999,
    publishedAt = null,
    recommended = false,
    resource = Resource.EntryComment,
    slug = "entry-comment-slug",
    source = null,
    tags = emptyList(),
    title = "",
    voted = voted,
    votes = votes(),
    favourite = false,
)

private fun author(): Author = Author(
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
)

private fun media(): Media = Media(
    embed = null,
    photo = null,
    survey = null,
)

private fun votes(): Votes = Votes(
    count = 0,
    down = 0,
    up = 0,
)

private fun actions(): Actions = Actions(
    create = false,
    createFavourite = false,
    delete = false,
    deleteFavourite = false,
    finishAma = false,
    report = false,
    startAma = false,
    undoVote = false,
    update = false,
    voteDown = true,
    voteUp = true,
)
