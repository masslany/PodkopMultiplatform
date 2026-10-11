package pl.masslany.podkop.test.entries

import pl.masslany.podkop.test.entries.EntryRoutes.ENTRIES_PATH
import pl.masslany.podkop.test.entries.EntryRoutes.entryText
import pl.masslany.podkop.test.fixtures.EntryThreadFixtures
import pl.masslany.podkop.test.fixtures.ResourceFixtures
import pl.masslany.podkop.test.support.MockApiServer

object EntryThreadRoutes {
    const val THREADS_PATH = "/api/v3/entries-threads"

    private val threadQuery = mapOf("sort" to "oldest", "limit" to "${EntryThreadFixtures.PAGE_SIZE}", "expanded" to "true")

    /**
     * An entry as a guest opens it with threaded comments on: the entry, and its thread, oldest
     * first. Scrolling past the first page loads the rest of the top-level comments, and the
     * first comment's further replies load on request.
     */
    fun MockApiServer.guestEntryThread(entryId: Int) {
        getJson(
            path = "$ENTRIES_PATH/$entryId",
            body = ResourceFixtures.withId(sample("entry-details-guest"), entryId, content = entryText(entryId)),
        )
        val thread = sample("entry-thread-guest")
        val replies = sample("entry-thread-replies-guest")
        getJson(
            path = "$THREADS_PATH/$entryId",
            query = mapOf(
                "comments_sort" to "oldest",
                "comments_limit" to "${EntryThreadFixtures.PAGE_SIZE}",
                "comments_expanded" to "true",
            ),
            body = EntryThreadFixtures.thread(thread, replies, entryId, entryText(entryId)),
        )
        getJson(
            path = "$THREADS_PATH/$entryId/comments",
            query = threadQuery + ("id" to "${EntryThreadFixtures.commentId(EntryThreadFixtures.PAGE_SIZE)}"),
            body = EntryThreadFixtures.laterComments(sample("entry-thread-comments-guest"), thread, replies, entryId),
        )
        val firstComment = EntryThreadFixtures.commentId(1)
        getJson(
            path = "$THREADS_PATH/$entryId/comments/$firstComment/comments",
            query = threadQuery + ("id" to "${EntryThreadFixtures.replyId(commentIndex = 1, index = 1)}"),
            body = EntryThreadFixtures.laterReplies(replies),
        )
    }
}
