package pl.masslany.podkop.features.resources.models.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Author
import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Comments
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.Media
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes
import pl.masslany.podkop.common.models.EntryContentState

class EntryMappersTest {

    @Test
    fun `entry mapper keeps content not downvoted even when voted negative`() {
        val state = entryResource(
            voted = Voted.Negative,
        ).toEntryItemState()

        val contentState = assertIs<EntryContentState.Content>(state.entryContentState)
        assertEquals(false, contentState.isDownVoted)
    }

    @Test
    fun `entry mapper keeps mapped comments not downvoted even when comment voted negative`() {
        val state = entryResource(
            comments = Comments(
                count = 1,
                hot = false,
                items = listOf(
                    entryComment(voted = Voted.Negative),
                ),
            ),
        ).toEntryItemState()

        val contentState = assertIs<EntryContentState.Content>(state.comments.single().entryContentState)
        assertEquals(false, contentState.isDownVoted)
    }

    @Test
    fun `entry mapper maps edit prefill fields`() {
        val state = entryResource(
            content = "edited entry raw",
            adult = true,
            editable = true,
            actions = actions(update = false),
            media = media(
                photo = photo(
                    key = "entry-photo-key",
                    url = "https://cdn.example/entry.jpg",
                ),
            ),
        ).toEntryItemState()

        assertEquals(true, state.isEditEnabled)
        assertEquals("edited entry raw", state.rawContent)
        assertEquals(true, state.adult)
        assertEquals("entry-photo-key", state.photoKey)
        assertEquals("https://cdn.example/entry.jpg", state.photoUrl)
    }

    @Test
    fun `entry mapper maps nested entry comment edit prefill fields`() {
        val state = entryResource(
            comments = Comments(
                count = 1,
                hot = false,
                items = listOf(
                    entryComment(
                        content = "edited comment raw",
                        adult = true,
                        editable = true,
                        actions = actions(update = false),
                        media = media(
                            photo = photo(
                                key = "comment-photo-key",
                                url = "https://cdn.example/comment.jpg",
                            ),
                        ),
                    ),
                ),
            ),
        ).toEntryItemState()

        val comment = state.comments.single()
        assertEquals(true, comment.isEditEnabled)
        assertEquals("edited comment raw", comment.rawContent)
        assertEquals(true, comment.adult)
        assertEquals("comment-photo-key", comment.embedImageState?.key)
        assertEquals("https://cdn.example/comment.jpg", comment.embedImageState?.url)
    }

    @Test
    fun `entry mapper derives reply capability from create action`() {
        val state = entryResource(
            actions = actions(create = true),
            comments = Comments(
                count = 1,
                hot = false,
                items = listOf(
                    entryComment(actions = actions(create = true)),
                ),
            ),
        ).toEntryItemState()

        assertEquals(true, state.isReplyEnabled)
        assertEquals(true, state.comments.single().isReplyEnabled)
    }
}

private fun entryResource(
    comments: Comments? = null,
    voted: Voted = Voted.None,
    actions: Actions = actions(),
    content: String = "entry",
    adult: Boolean = false,
    editable: Boolean = false,
    media: Media = media(),
): ResourceItem = ResourceItem(
    actions = actions,
    adult = adult,
    archive = false,
    author = null,
    comments = comments,
    content = content,
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = editable,
    hot = false,
    id = 1,
    media = media,
    name = "",
    parent = null,
    parentId = null,
    publishedAt = null,
    recommended = false,
    resource = Resource.Entry,
    slug = "entry-slug",
    source = null,
    tags = emptyList(),
    title = "",
    voted = voted,
    votes = votes(),
    favourite = false,
)

private fun entryComment(
    voted: Voted = Voted.None,
    content: String = "comment",
    adult: Boolean = false,
    editable: Boolean = false,
    actions: Actions = actions(),
    media: Media = media(),
): Comment = Comment(
    actions = actions,
    adult = adult,
    archive = false,
    author = author(),
    blacklist = false,
    comments = null,
    content = content,
    createdAt = null,
    deletable = false,
    deleted = Deleted.None,
    device = "",
    editable = editable,
    favourite = false,
    id = 2,
    media = media,
    parentId = 1,
    resource = Resource.EntryComment,
    slug = "comment-slug",
    tags = emptyList(),
    voted = voted,
    votes = votes(),
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
    voteDown = true,
    voteUp = true,
    vote = false,
)
