package pl.masslany.podkop.features.resources

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
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
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.models.withFavourite
import pl.masslany.podkop.testsupport.fakes.FakeDispatcherProvider

class ResourceItemsStoreTest {

    private val sut = ResourceItemsStore(FakeDispatcherProvider(), isUpcoming = false)

    @Test
    fun `an update reaches a comment shown under its entry`() = runTest {
        sut.replaceAll(listOf(resource(1, Resource.Entry, comments = comments(comment(2, Resource.EntryComment)))))

        sut.update(2) { it.withFavourite(true) }

        val entry = assertIs<EntryItemState>(sut.items.value.single())
        assertTrue(entry.comments.single().isFavourite)
    }

    @Test
    fun `an update reaches a reply under a link comment`() = runTest {
        val reply = comment(11, Resource.LinkComment)
        sut.replaceAll(listOf(resource(10, Resource.LinkComment, comments = comments(reply))))

        sut.updateAs<LinkCommentItemState>(11) { it.copy(isFavourite = true) }

        val comment = assertIs<LinkCommentItemState>(sut.items.value.single())
        assertTrue(comment.replies.single().isFavourite)
    }

    @Test
    fun `replace swaps only the item with the resource's id`() = runTest {
        sut.replaceAll(listOf(resource(1, Resource.Entry), resource(3, Resource.Entry)))

        sut.replace(resource(1, Resource.Entry).copy(content = "fresh"))

        assertEquals(listOf("fresh", "c3"), sut.items.value.map { (it as EntryItemState).rawContent })
    }

    @Test
    fun `an edited entry comment keeps the parent it is listed under`() = runTest {
        sut.replaceAll(listOf(resource(2, Resource.EntryComment, parent = Parent(id = 1))))

        sut.applyEdited(resource(2, Resource.EntryComment).copy(content = "edited"))

        val comment = assertIs<EntryCommentItemState>(sut.items.value.single())
        assertEquals("edited", comment.rawContent)
        assertEquals(1, comment.parentId)
    }

    @Test
    fun `an entry comment is found listed or under its entry`() = runTest {
        sut.replaceAll(
            listOf(
                resource(1, Resource.Entry, comments = comments(comment(2, Resource.EntryComment))),
                resource(5, Resource.EntryComment),
            ),
        )

        assertEquals(2, sut.findEntryComment(entryId = 1, commentId = 2)?.id)
        assertEquals(5, sut.findEntryComment(entryId = 1, commentId = 5)?.id)
        assertNull(sut.findEntryComment(entryId = 3, commentId = 2))
    }

    private fun resource(
        id: Int,
        resource: Resource,
        comments: Comments? = null,
        parent: Parent? = null,
    ) = ResourceItem(
        actions = null, adult = false, archive = false, author = null, comments = comments,
        content = "c$id", createdAt = null, deleted = Deleted.None, deletable = false,
        description = "", editable = false, hot = false, id = id, media = null, name = "",
        parent = parent, publishedAt = null, recommended = false, resource = resource, slug = "",
        source = null, tags = emptyList(), title = "", voted = Voted.None, votes = null, favourite = false,
    )

    private fun comments(vararg items: Comment) = Comments(count = items.size, hot = false, items = items.toList())

    private fun comment(id: Int, resource: Resource) = Comment(
        actions = Actions(
            create = false, createFavourite = false, delete = false, deleteFavourite = false, finishAma = false,
            report = false, startAma = false, undoVote = false, update = false, voteDown = false, voteUp = false,
            vote = false,
        ),
        adult = false, archive = false, blacklist = false, comments = null, content = "c$id", createdAt = null,
        author = Author(
            avatar = "", blacklist = false, color = NameColor.Orange, company = false, follow = false,
            gender = Gender.Unspecified, note = false, online = false, rank = Rank(position = 0, trend = 0),
            status = "active", username = "tester", verified = false,
        ),
        deletable = false, deleted = Deleted.None, device = "", editable = false, favourite = false, id = id,
        media = Media(embed = null, photo = null, survey = null), parentId = 0, resource = resource, slug = "",
        tags = emptyList(), voted = Voted.None, votes = Votes(count = 0, down = 0, up = 0),
    )
}
