package pl.masslany.podkop.features.resources.models.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

class LinkMappersTest {

    @Test
    fun `link mapper passes root id and slug to top level comment previews`() {
        val linkId = 7896627
        val linkSlug = "emulator-x86-napisany-w-czystym-css"
        val state = linkResource(
            id = linkId,
            slug = linkSlug,
            comments = comments(
                linkComment(
                    id = 135740393,
                    parentId = linkId,
                    slug = "komentarz",
                ),
            ),
        ).toLinkItemState(isUpcoming = false)

        val comment = state.comments.single()
        assertEquals(linkId, comment.linkId)
        assertEquals(linkSlug, comment.linkSlug)
        assertNull(comment.parentCommentIdOrNull)
    }

    @Test
    fun `link mapper keeps root slug when preview item is a reply`() {
        val linkId = 7896627
        val linkSlug = "emulator-x86-napisany-w-czystym-css"
        val state = linkResource(
            id = linkId,
            slug = linkSlug,
            comments = comments(
                linkComment(
                    id = 135740683,
                    parentId = 135740393,
                    slug = "komentarz",
                ),
            ),
        ).toLinkItemState(isUpcoming = false)

        val reply = state.comments.single()
        assertEquals(linkId, reply.linkId)
        assertEquals(linkSlug, reply.linkSlug)
        assertEquals(135740393, reply.parentCommentIdOrNull)
    }

    @Test
    fun `link mapper returns empty comments list when source comments are null`() {
        val state = linkResource(
            id = 7896627,
            slug = "emulator-x86-napisany-w-czystym-css",
            comments = null,
        ).toLinkItemState(isUpcoming = false)

        assertEquals(0, state.comments.size)
        assertEquals(0, state.commentCount)
    }

    @Test
    fun `link mapper exposes downvote availability and selected state for negative votes`() {
        val state = linkResource(
            id = 42,
            slug = "link",
            comments = null,
            actions = actions(voteDown = false, undoVote = true),
            voted = Voted.Negative,
        ).toLinkItemState(isUpcoming = false)

        assertTrue(state.canVoteDown)
        assertTrue(state.isDownVoted)
        assertTrue(state.countState.canVote)
        assertTrue(state.countState.isVoted)
    }

    @Test
    fun `link mapper keeps downvote button hidden when negative vote is unavailable`() {
        val state = linkResource(
            id = 42,
            slug = "link",
            comments = null,
            actions = actions(voteDown = false, undoVote = false),
        ).toLinkItemState(isUpcoming = false)

        assertFalse(state.canVoteDown)
    }
}

private fun linkResource(
    id: Int,
    slug: String,
    comments: Comments?,
    actions: Actions = actions(),
    voted: Voted = Voted.None,
): ResourceItem = ResourceItem(
    actions = actions,
    adult = false,
    archive = false,
    author = null,
    comments = comments,
    content = "",
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "desc",
    editable = false,
    hot = false,
    id = id,
    media = null,
    name = "",
    parent = null,
    parentId = null,
    publishedAt = null,
    recommended = false,
    resource = Resource.Link,
    slug = slug,
    source = null,
    tags = emptyList(),
    title = "title",
    voted = voted,
    votes = Votes(count = 0, down = 0, up = 0),
    favourite = false,
)

private fun linkComment(
    id: Int,
    parentId: Int,
    slug: String,
): Comment = Comment(
    actions = actions(),
    adult = false,
    archive = false,
    author = author(),
    blacklist = false,
    comments = null,
    content = "comment",
    createdAt = null,
    deletable = false,
    deleted = Deleted.None,
    device = "",
    editable = false,
    favourite = false,
    id = id,
    media = media(),
    parentId = parentId,
    resource = Resource.LinkComment,
    slug = slug,
    tags = emptyList(),
    voted = Voted.None,
    votes = votes(),
)

private fun comments(vararg items: Comment): Comments = Comments(
    count = items.size,
    hot = false,
    items = items.toList(),
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

private fun actions(
    undoVote: Boolean = false,
    voteDown: Boolean = false,
    voteUp: Boolean = false,
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
