package pl.masslany.podkop.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Parent
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies

class ThreadMappingTest {
    @Test
    fun mapsThreadCommentsWithTheirReplyTreeAndEntryParent() {
        val reply = EntryThreadComment(
            comment = comment(id = 11),
            depth = 1,
            isByEntryAuthor = true,
            replyParentId = 11,
            replies = EntryThreadReplies(totalCount = 0, items = emptyList()),
        )
        val replies = EntryThreadReplies(
            totalCount = 40,
            items = listOf(
                EntryThreadComment(
                    comment = comment(id = 10),
                    depth = 0,
                    isByEntryAuthor = false,
                    replyParentId = 10,
                    replies = EntryThreadReplies(totalCount = 3, items = listOf(reply)),
                ),
            ),
        )

        val mapped = replies.toIOSThreadReplies()

        assertEquals(40, mapped.totalCount)
        val top = mapped.items.single()
        assertEquals(10, top.resource.id)
        assertEquals("entryComment", top.resource.kind)
        assertEquals(0, top.depth)
        assertEquals(3, top.replies.totalCount)
        val nested = top.replies.items.single()
        assertEquals(1, nested.depth)
        assertTrue(nested.isByEntryAuthor)
        assertEquals(11, nested.replyParentId)
        // Actions on entry comments address them under their entry.
        assertEquals(1, nested.resource.parentId)
    }

    private fun comment(id: Int) = ResourceItem(
        actions = null, adult = false, archive = false, author = null, comments = null,
        content = "c$id", createdAt = null, deleted = Deleted.None, deletable = false,
        description = "", editable = false, hot = false, id = id, media = null, name = "",
        parent = Parent(id = 1), parentId = 1, publishedAt = null, recommended = false,
        resource = Resource.EntryComment, slug = "", source = null, tags = emptyList(),
        title = "", voted = Voted.None, votes = null, favourite = false,
    )
}
