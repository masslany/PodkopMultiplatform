package pl.masslany.podkop.features.resources.models.linkcomment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

class LinkCommentMappersTest {

    @Test
    fun `resource mapper uses parent id as link id and own slug for top level link comment`() {
        val state = resourceLinkComment(
            id = 135740393,
            slug = "emulator-x86-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
        ).toLinkCommentItemState()

        assertEquals(7896627, state.linkId)
        assertEquals("emulator-x86-slug", state.linkSlug)
        assertNull(state.parentCommentIdOrNull)
    }

    @Test
    fun `resource mapper prefers parent linkId over parent id for reply resources`() {
        val state = resourceLinkComment(
            id = 135742563,
            slug = "komentarz",
            parent = Parent(id = 135726609, linkId = 7896049),
            parentId = 135726609,
        ).toLinkCommentItemState()

        assertEquals(7896049, state.linkId)
        assertEquals("komentarz", state.linkSlug)
        assertEquals(135726609, state.parentCommentIdOrNull)
    }

    @Test
    fun `resource mapper applies overrides and propagates root link slug to nested replies`() {
        val rootLinkSlug = "emulator-x86-css-bez-javascriptu"
        val state = resourceLinkComment(
            id = 135726609,
            slug = "komentarz",
            parent = Parent(id = 7896049),
            parentId = 7896049,
            comments = comments(
                commentLinkReply(
                    id = 135742563,
                    parentId = 135726609,
                    slug = "komentarz",
                ),
            ),
        ).toLinkCommentItemState(
            linkIdOverride = 7896049,
            linkSlugOverride = rootLinkSlug,
        )

        val reply = state.replies.single()
        assertEquals(rootLinkSlug, state.linkSlug)
        assertEquals(rootLinkSlug, reply.linkSlug)
        assertEquals(7896049, reply.linkId)
        assertEquals(135726609, reply.parentCommentIdOrNull)
    }

    @Test
    fun `resource mapper override link id takes precedence over parent link id`() {
        val state = resourceLinkComment(
            id = 135742563,
            slug = "komentarz",
            parent = Parent(id = 135726609, linkId = 1111111),
            parentId = 135726609,
        ).toLinkCommentItemState(
            linkIdOverride = 7896049,
            linkSlugOverride = "real-link-slug",
        )

        assertEquals(7896049, state.linkId)
        assertEquals("real-link-slug", state.linkSlug)
        assertEquals(135726609, state.parentCommentIdOrNull)
    }

    @Test
    fun `comment mapper uses provided link slug instead of comment slug`() {
        val state = commentLinkReply(
            id = 135740393,
            parentId = 7896627,
            slug = "komentarz",
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "emulator-x86-css-bez-javascriptu",
        )

        assertEquals(7896627, state.linkId)
        assertEquals("emulator-x86-css-bez-javascriptu", state.linkSlug)
        assertNull(state.parentCommentIdOrNull)
    }

    @Test
    fun `comment mapper marks reply parent comment id for deep link url building`() {
        val state = commentLinkReply(
            id = 135740683,
            parentId = 135740393,
            slug = "komentarz",
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "emulator-x86-css-bez-javascriptu",
        )

        assertEquals(135740393, state.parentCommentIdOrNull)
    }

    @Test
    fun `resource mapper falls back to zero link id when parent and override are missing`() {
        val state = resourceLinkComment(
            id = 123,
            slug = "fallback-slug",
            parent = null,
            parentId = null,
        ).toLinkCommentItemState()

        assertEquals(0, state.linkId)
        assertEquals("fallback-slug", state.linkSlug)
        assertNull(state.parentCommentIdOrNull)
    }

    @Test
    fun `resource mapper keeps empty replies when nested comments are missing`() {
        val state = resourceLinkComment(
            id = 135740393,
            slug = "comment-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
            comments = null,
        ).toLinkCommentItemState()

        assertEquals(0, state.replies.size)
    }

    @Test
    fun `parentCommentIdOrNull returns null when parent id equals comment id`() {
        val state = commentLinkReply(
            id = 135740393,
            parentId = 135740393,
            slug = "komentarz",
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "real-link-slug",
        )

        assertNull(state.parentCommentIdOrNull)
    }

    @Test
    fun `regression reply resource slug komentarz can be overridden with root link slug`() {
        val state = resourceLinkComment(
            id = 135742563,
            slug = "komentarz",
            parent = Parent(id = 135726609, linkId = 7896049),
            parentId = 135726609,
        ).toLinkCommentItemState(
            linkSlugOverride = "emulator-x86-napisany-w-czystym-css",
        )

        assertEquals(7896049, state.linkId)
        assertEquals("emulator-x86-napisany-w-czystym-css", state.linkSlug)
        assertEquals(135726609, state.parentCommentIdOrNull)
    }
}

private fun resourceLinkComment(
    id: Int,
    slug: String,
    parent: Parent?,
    parentId: Int?,
    comments: Comments? = null,
): ResourceItem = ResourceItem(
    actions = actions(),
    adult = false,
    archive = false,
    author = null,
    comments = comments,
    content = "test",
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = false,
    hot = false,
    id = id,
    media = media(),
    name = "",
    parent = parent,
    parentId = parentId,
    publishedAt = null,
    recommended = false,
    resource = Resource.LinkComment,
    slug = slug,
    source = null,
    tags = emptyList(),
    title = "",
    voted = Voted.None,
    votes = votes(),
)

private fun commentLinkReply(
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
    content = "reply",
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
    voteDown = false,
    voteUp = false,
)
