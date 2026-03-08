package pl.masslany.podkop.features.linkdetails

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkDetailsScreenRootTest {

    @Test
    fun `current user accent has highest priority`() {
        val result = resolveLinkCommentAccent(
            commentAuthor = "patryk",
            linkAuthorName = "patryk",
            parentCommentAuthorName = "patryk",
            currentUsername = "patryk",
        )

        assertEquals(LinkCommentAccent.CurrentUser, result)
    }

    @Test
    fun `link author accent wins over parent author accent`() {
        val result = resolveLinkCommentAccent(
            commentAuthor = "link_author",
            linkAuthorName = "link_author",
            parentCommentAuthorName = "link_author",
            currentUsername = "patryk",
        )

        assertEquals(LinkCommentAccent.LinkAuthor, result)
    }

    @Test
    fun `reply from parent author gets parent accent`() {
        val result = resolveLinkCommentAccent(
            commentAuthor = "thread_parent",
            linkAuthorName = "link_author",
            parentCommentAuthorName = "thread_parent",
            currentUsername = "patryk",
        )

        assertEquals(LinkCommentAccent.ParentAuthor, result)
    }

    @Test
    fun `unmatched author has no accent`() {
        val result = resolveLinkCommentAccent(
            commentAuthor = "someone_else",
            linkAuthorName = "link_author",
            parentCommentAuthorName = "thread_parent",
            currentUsername = "patryk",
        )

        assertNull(result)
    }

    @Test
    fun `blank comment author has no accent`() {
        val result = resolveLinkCommentAccent(
            commentAuthor = "",
            linkAuthorName = "link_author",
            parentCommentAuthorName = "thread_parent",
            currentUsername = "patryk",
        )

        assertNull(result)
    }
}
