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
import pl.masslany.podkop.business.common.domain.models.common.Photo
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

    @Test
    fun `entry comment mapper maps edit prefill fields`() {
        val state = entryCommentResource(
            content = "entry comment raw content",
            adult = true,
            editable = true,
            actions = actions(update = false),
            media = media(
                photo = photo(
                    key = "comment-photo-key",
                    url = "https://cdn.example/entry-comment.jpg",
                ),
            ),
        ).toEntryCommentItemState()

        assertEquals(true, state.isEditEnabled)
        assertEquals("entry comment raw content", state.rawContent)
        assertEquals(true, state.adult)
        assertEquals("comment-photo-key", state.embedImageState?.key)
        assertEquals("https://cdn.example/entry-comment.jpg", state.embedImageState?.url)
    }

    @Test
    fun `entry comment mapper hides blacklisted content`() {
        val state = entryCommentResource(
            author = author(blacklist = true),
            content = "hidden entry comment",
        ).toEntryCommentItemState()

        assertEquals(true, state.isBlacklisted)
        assertIs<EntryContentState.Content>(state.entryContentState)
    }

    @Test
    fun `entry comment mapper shows deleted by entry author state for host deleted comments`() {
        val state = entryCommentResource(
            deleted = Deleted.Host,
            content = "should not be visible",
        ).toEntryCommentItemState()

        assertEquals(EntryContentState.DeletedByEntryAuthor, state.entryContentState)
    }

    @Test
    fun `entry comment mapper derives reply capability from create action`() {
        val state = entryCommentResource(
            actions = actions(create = true),
        ).toEntryCommentItemState()

        assertEquals(true, state.isReplyEnabled)
    }
}

private fun entryCommentResource(
    voted: Voted = Voted.None,
    content: String = "entry comment",
    adult: Boolean = false,
    editable: Boolean = false,
    actions: Actions = actions(),
    media: Media = media(),
    author: Author = author(),
    deleted: Deleted = Deleted.None,
): ResourceItem = ResourceItem(
    actions = actions,
    adult = adult,
    archive = false,
    author = author,
    comments = null,
    content = content,
    createdAt = null,
    deleted = deleted,
    deletable = false,
    description = "",
    editable = editable,
    hot = false,
    id = 1,
    media = media,
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
    voteDown = true,
    voteUp = true,
    vote = false,
)
