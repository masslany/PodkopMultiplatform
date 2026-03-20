package pl.masslany.podkop.features.resources.models.linkcomment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes
import pl.masslany.podkop.common.models.EntryContentState

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
    fun `resource mapper maps edit prefill fields`() {
        val state = resourceLinkComment(
            id = 135740393,
            slug = "comment-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
            content = "link comment raw content",
            adult = true,
            editable = true,
            actions = actions(update = false),
            media = media(
                photo = photo(
                    key = "link-comment-photo-key",
                    url = "https://cdn.example/link-comment.jpg",
                ),
            ),
        ).toLinkCommentItemState()

        assertEquals(true, state.isEditEnabled)
        assertEquals("link comment raw content", state.rawContent)
        assertEquals(true, state.adult)
        assertEquals("link-comment-photo-key", state.embedImageState?.key)
        assertEquals("https://cdn.example/link-comment.jpg", state.embedImageState?.url)
    }

    @Test
    fun `resource mapper marks content as downvoted when vote is negative`() {
        val state = resourceLinkComment(
            id = 135740393,
            slug = "comment-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
            voted = Voted.Negative,
            votes = Votes(count = 2, down = 2, up = 0),
        ).toLinkCommentItemState()

        val contentState = assertIs<EntryContentState.Content>(state.entryContentState)
        assertEquals(true, contentState.isDownVoted)
    }

    @Test
    fun `comment mapper marks content as downvoted when vote is negative`() {
        val state = commentLinkReply(
            id = 135740683,
            parentId = 135740393,
            slug = "komentarz",
            voted = Voted.Negative,
            votes = Votes(count = 2, down = 2, up = 0),
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "emulator-x86-css-bez-javascriptu",
        )

        val contentState = assertIs<EntryContentState.Content>(state.entryContentState)
        assertEquals(true, contentState.isDownVoted)
    }

    @Test
    fun `resource mapper hides blacklisted comment content`() {
        val state = resourceLinkComment(
            id = 135740393,
            slug = "comment-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
            author = author(blacklist = true),
        ).toLinkCommentItemState()

        assertEquals(true, state.isBlacklisted)
        assertIs<EntryContentState.Content>(state.entryContentState)
    }

    @Test
    fun `comment mapper hides blacklisted reply content`() {
        val state = commentLinkReply(
            id = 135740683,
            parentId = 135740393,
            slug = "komentarz",
            blacklist = true,
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "emulator-x86-css-bez-javascriptu",
        )

        assertEquals(true, state.isBlacklisted)
        assertIs<EntryContentState.Content>(state.entryContentState)
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

    @Test
    fun `link comment mapper derives reply capability from create action`() {
        val resourceState = resourceLinkComment(
            id = 135740393,
            slug = "comment-slug",
            parent = Parent(id = 7896627),
            parentId = 7896627,
            actions = actions(create = true),
        ).toLinkCommentItemState()

        val replyState = commentLinkReply(
            id = 135740683,
            parentId = 135740393,
            slug = "komentarz",
            actions = actions(create = true),
        ).toLinkCommentItemState(
            linkId = 7896627,
            linkSlug = "emulator-x86-css-bez-javascriptu",
        )

        assertEquals(true, resourceState.isReplyEnabled)
        assertEquals(true, replyState.isReplyEnabled)
    }
}

private fun resourceLinkComment(
    id: Int,
    slug: String,
    parent: Parent?,
    parentId: Int?,
    comments: Comments? = null,
    voted: Voted = Voted.None,
    votes: Votes = votes(),
    content: String = "test",
    adult: Boolean = false,
    editable: Boolean = false,
    actions: Actions = actions(),
    media: Media = media(),
    author: Author? = null,
): ResourceItem = ResourceItem(
    actions = actions,
    adult = adult,
    archive = false,
    author = author,
    comments = comments,
    content = content,
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = editable,
    hot = false,
    id = id,
    media = media,
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
    voted = voted,
    votes = votes,
    favourite = false,
)

private fun commentLinkReply(
    id: Int,
    parentId: Int,
    slug: String,
    voted: Voted = Voted.None,
    votes: Votes = votes(),
    content: String = "reply",
    adult: Boolean = false,
    editable: Boolean = false,
    actions: Actions = actions(),
    media: Media = media(),
    blacklist: Boolean = false,
): Comment = Comment(
    actions = actions,
    adult = adult,
    archive = false,
    author = author(),
    blacklist = blacklist,
    comments = null,
    content = content,
    createdAt = null,
    deletable = false,
    deleted = Deleted.None,
    device = "",
    editable = editable,
    favourite = false,
    id = id,
    media = media,
    parentId = parentId,
    resource = Resource.LinkComment,
    slug = slug,
    tags = emptyList(),
    voted = voted,
    votes = votes,
)

private fun comments(vararg items: Comment): Comments = Comments(
    count = items.size,
    hot = false,
    items = items.toList(),
)

private fun author(blacklist: Boolean = false): Author = Author(
    avatar = "",
    blacklist = blacklist,
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

private fun media(photo: Photo? = null): Media = Media(
    embed = null,
    photo = photo,
    survey = null,
)

private fun photo(
    key: String,
    url: String,
): Photo = Photo(
    height = 100,
    key = key,
    label = "",
    mimeType = "image/jpeg",
    size = 1,
    url = url,
    width = 100,
)

private fun votes(): Votes = Votes(
    count = 0,
    down = 0,
    up = 0,
)

private fun actions(
    create: Boolean = false,
    update: Boolean = false,
): Actions = Actions(
    create = create,
    createFavourite = false,
    delete = false,
    deleteFavourite = false,
    finishAma = false,
    report = false,
    startAma = false,
    undoVote = false,
    update = update,
    voteDown = false,
    voteUp = false,
)
