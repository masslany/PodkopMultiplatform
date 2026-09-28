package pl.masslany.podkop.features.entrydetails

import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.features.entrydetails.EntryCommentThreadTree.Row
import pl.masslany.podkop.features.resources.models.toResourceItemState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EntryCommentThreadTreeTest {

    @Test
    fun `rows walk the tree depth first with more-replies rows after partial branches`() {
        val sut = EntryCommentThreadTree.from(
            replies(
                total = 3,
                comment(10, depth = 0, total = 5, replies = listOf(comment(11, depth = 1), comment(12, depth = 1))),
                comment(20, depth = 0, isByEntryAuthor = true),
            ),
        )

        assertEquals(
            listOf(
                Row.Comment(id = 10, depth = 0, isByEntryAuthor = false),
                Row.Comment(id = 11, depth = 1, isByEntryAuthor = false),
                Row.Comment(id = 12, depth = 1, isByEntryAuthor = false),
                Row.MoreReplies(parentId = 10, depth = 1, remainingCount = 3),
                Row.Comment(id = 20, depth = 0, isByEntryAuthor = true),
            ),
            sut.rows(),
        )
        assertTrue(sut.hasMoreTopLevel)
        assertEquals(20, sut.topLevelCursorId)
        assertEquals(12, sut.repliesCursorId(10))
    }

    @Test
    fun `appending a replies page extends the branch and moves its cursor`() {
        val sut = EntryCommentThreadTree.from(
            replies(total = 1, comment(10, depth = 0, total = 3, replies = listOf(comment(11, depth = 1)))),
        )

        val actual = sut.appendPage(
            parentId = 10,
            page = replies(total = 3, comment(12, depth = 1), comment(13, depth = 1, total = 1)),
        )

        assertEquals(
            listOf(
                Row.Comment(id = 10, depth = 0, isByEntryAuthor = false),
                Row.Comment(id = 11, depth = 1, isByEntryAuthor = false),
                Row.Comment(id = 12, depth = 1, isByEntryAuthor = false),
                Row.Comment(id = 13, depth = 1, isByEntryAuthor = false),
                Row.MoreReplies(parentId = 13, depth = 2, remainingCount = 1),
            ),
            actual.rows(),
        )
        assertEquals(13, actual.repliesCursorId(10))
    }

    @Test
    fun `appending a page skips comments that are already loaded`() {
        val sut = EntryCommentThreadTree.from(replies(total = 2, comment(10, depth = 0)))

        val actual = sut.appendPage(parentId = null, page = replies(total = 2, comment(10, depth = 0), comment(20, depth = 0)))

        assertEquals(listOf(10, 20), actual.rows().map { (it as Row.Comment).id })
        assertFalse(actual.hasMoreTopLevel)
    }

    @Test
    fun `posted replies go last without moving the server cursor`() {
        val sut = EntryCommentThreadTree.from(
            replies(total = 1, comment(10, depth = 0, total = 4, replies = listOf(comment(11, depth = 1)))),
        )

        val actual = sut.appendPosted(parentId = 10, commentId = 99, isByEntryAuthor = true)

        assertEquals(
            listOf(
                Row.Comment(id = 10, depth = 0, isByEntryAuthor = false),
                Row.Comment(id = 11, depth = 1, isByEntryAuthor = false),
                Row.Comment(id = 99, depth = 1, isByEntryAuthor = true),
                Row.MoreReplies(parentId = 10, depth = 1, remainingCount = 3),
            ),
            actual.rows(),
        )
        assertEquals(11, actual.repliesCursorId(10))
        assertEquals(99, actual.replyParentId(99))
    }

    @Test
    fun `posted top-level comment is appended under the entry`() {
        val sut = EntryCommentThreadTree.from(replies(total = 1, comment(10, depth = 0)))

        val actual = sut.appendPosted(parentId = null, commentId = 50, isByEntryAuthor = false)

        assertEquals(listOf(10, 50), actual.rows().map { (it as Row.Comment).id })
        assertFalse(actual.hasMoreTopLevel)
        assertEquals(10, actual.topLevelCursorId)
    }

    @Test
    fun `posted reply at the depth limit sends further replies to its parent`() {
        val sut = EntryCommentThreadTree.from(replies(total = 1, comment(10, depth = 4)))

        val actual = sut.appendPosted(parentId = 10, commentId = 11, isByEntryAuthor = false)

        assertEquals(10, actual.replyParentId(11))
    }

    @Test
    fun `reply parent comes from the server and is unknown for unloaded comments`() {
        val sut = EntryCommentThreadTree.from(replies(total = 1, comment(10, depth = 5, replyParentId = 9)))

        assertEquals(9, sut.replyParentId(10))
        assertNull(sut.replyParentId(404))
    }

    @Test
    fun `thread rows resolve holder states and mark loading branches`() {
        val tree = EntryCommentThreadTree.from(
            replies(total = 2, comment(10, depth = 0, total = 2), comment(20, depth = 0)),
        )
        val states = listOf(resource(10).toResourceItemState())

        val actual = buildEntryThreadRows(tree, states, loadingRepliesIds = setOf(10))

        assertEquals(
            listOf(
                EntryThreadRowState.Comment(item = states.single(), depth = 0, isByEntryAuthor = false),
                EntryThreadRowState.MoreReplies(parentId = 10, depth = 1, remainingCount = 2, isLoading = true),
            ),
            actual,
        )
    }

    @Test
    fun `flatten lists parents before their replies`() {
        val page = replies(
            total = 2,
            comment(10, depth = 0, replies = listOf(comment(11, depth = 1, replies = listOf(comment(12, depth = 2))))),
            comment(20, depth = 0),
        )

        assertEquals(listOf(10, 11, 12, 20), page.flattenComments().map { it.id })
    }

    private fun replies(total: Int, vararg items: EntryThreadComment) =
        EntryThreadReplies(totalCount = total, items = items.toList())

    @Suppress("LongParameterList")
    private fun comment(
        id: Int,
        depth: Int,
        total: Int? = null,
        replies: List<EntryThreadComment> = emptyList(),
        isByEntryAuthor: Boolean = false,
        replyParentId: Int = id,
    ) = EntryThreadComment(
        comment = resource(id),
        depth = depth,
        isByEntryAuthor = isByEntryAuthor,
        replyParentId = replyParentId,
        replies = EntryThreadReplies(totalCount = total ?: replies.size, items = replies),
    )

    private fun resource(id: Int) = ResourceItem(
        actions = null,
        adult = false,
        archive = false,
        author = null,
        comments = null,
        content = "comment $id",
        createdAt = null,
        deleted = Deleted.None,
        deletable = false,
        description = "",
        editable = false,
        hot = false,
        id = id,
        media = null,
        name = "",
        parent = null,
        publishedAt = null,
        recommended = false,
        resource = Resource.EntryComment,
        slug = "",
        source = null,
        tags = emptyList(),
        title = "",
        voted = Voted.None,
        votes = null,
        favourite = false,
    )
}
