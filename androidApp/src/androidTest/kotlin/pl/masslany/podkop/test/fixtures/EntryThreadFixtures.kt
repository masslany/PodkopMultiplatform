package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds an entry's comment threads from sanitized samples of real responses (captured as a guest,
 * 2026-10-10), changing only values:
 *
 * - `entries-threads/{id}` serves the entry with its first page of top-level comments under
 *   `comments: {count, total, items}`, where `count` counts top-level comments only. Comments nest
 *   their replies the same way, and a reply list can be partly inlined: `count: 2` with one item.
 * - Later top-level comments and a comment's further replies come from `.../comments` and
 *   `.../comments/{id}/comments` as `data: {count, total, items}`, with no pagination block. Each
 *   page starts after the `id` the app sends, the last comment it has.
 */
object EntryThreadFixtures {
    /** The app asks for 25 comments at a time. */
    const val PAGE_SIZE = 25

    /** The first comment has two replies, of which the thread inlines one. */
    const val FIRST_COMMENT_REPLIES = 2

    fun commentId(index: Int): Int = 20_000 + index

    fun commentText(index: Int): String = "Thread comment $index"

    fun replyId(
        commentIndex: Int,
        index: Int,
    ): Int = commentId(commentIndex) * 10 + index

    fun replyText(
        commentIndex: Int,
        index: Int,
    ): String = "Thread reply $commentIndex-$index"

    /** Top-level comments in the thread, as the sample counts them. */
    fun topLevelCount(thread: JsonObject): Int = thread.entry().comments().getValue("count").jsonPrimitive.int

    /** The thread of entry [entryId]: the entry, with content [entryText], and its first page. */
    fun thread(
        thread: JsonObject,
        replies: JsonObject,
        entryId: Int,
        entryText: String,
    ): String {
        val entry = thread.entry()
        val comments = (1..minOf(PAGE_SIZE, topLevelCount(thread))).map { index ->
            topLevel(thread, replies, entryId, index)
        }
        return thread.with(
            "data" to entry.withoutImages().with(
                "id" to JsonPrimitive(entryId),
                "content" to JsonPrimitive(entryText),
                "comments" to entry.comments().with("items" to JsonArray(comments)),
            ),
        ).toString()
    }

    /** The top-level comments after the first page, as `.../comments?id=` answers them. */
    fun laterComments(
        sample: JsonObject,
        thread: JsonObject,
        replies: JsonObject,
        entryId: Int,
    ): String {
        val items = (PAGE_SIZE + 1..topLevelCount(thread)).map { index -> topLevel(thread, replies, entryId, index) }
        return sample.with("data" to sample.getValue("data").jsonObject.with("items" to JsonArray(items))).toString()
    }

    /** The first comment's replies after the inlined one, as `.../comments/{id}/comments?id=` answers them. */
    fun laterReplies(replies: JsonObject): String {
        val items = (2..FIRST_COMMENT_REPLIES).map { index -> reply(replies, commentIndex = 1, index = index) }
        return replies.with(
            "data" to replies.getValue("data").jsonObject.with(
                "count" to JsonPrimitive(FIRST_COMMENT_REPLIES),
                "total" to JsonPrimitive(FIRST_COMMENT_REPLIES),
                "items" to JsonArray(items),
            ),
        ).toString()
    }

    private fun topLevel(
        thread: JsonObject,
        replies: JsonObject,
        entryId: Int,
        index: Int,
    ): JsonObject {
        val sampleComments = thread.entry().comments().getValue("items").jsonArray.map { it.jsonObject }
        // The sample's first comment has no replies; its second has an inlined one.
        val template = if (index == 1) sampleComments[1] else sampleComments[0]
        val ownComments = template.comments()
        val nested = if (index == 1) {
            ownComments.with(
                "count" to JsonPrimitive(FIRST_COMMENT_REPLIES),
                "total" to JsonPrimitive(FIRST_COMMENT_REPLIES),
                "items" to JsonArray(listOf(reply(replies, commentIndex = 1, index = 1))),
            )
        } else {
            ownComments
        }
        return template.withoutImages().with(
            "id" to JsonPrimitive(commentId(index)),
            "content" to JsonPrimitive(commentText(index)),
            "parent" to template.getValue("parent").jsonObject.withoutImages().with("id" to JsonPrimitive(entryId)),
            "comments" to nested,
        )
    }

    private fun reply(
        replies: JsonObject,
        commentIndex: Int,
        index: Int,
    ): JsonObject {
        // A reply without replies of its own.
        val template = replies.getValue("data").jsonObject.getValue("items").jsonArray.first().jsonObject
        return template.withoutImages().with(
            "id" to JsonPrimitive(replyId(commentIndex, index)),
            "content" to JsonPrimitive(replyText(commentIndex, index)),
            "parent" to template.getValue("parent").jsonObject.withoutImages().with(
                "id" to JsonPrimitive(commentId(commentIndex)),
            ),
        )
    }

    private fun JsonObject.entry(): JsonObject = getValue("data").jsonObject

    private fun JsonObject.comments(): JsonObject = getValue("comments").jsonObject
}
