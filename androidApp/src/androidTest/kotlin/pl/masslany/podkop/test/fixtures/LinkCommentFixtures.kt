package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds a link's comments from sanitized samples of real responses (captured 2026-10-09), changing
 * only values:
 *
 * - Top-level comments come in numbered pages (`per_page: 25`, `total` counting top-level comments,
 *   `total_items` including replies). Each carries its reply `count` and inlines its first two replies.
 * - A comment's replies (`comments/{id}/comments`) come in numbered pages of 50, starting from the
 *   first reply, so the first page repeats the inlined ones.
 */
object LinkCommentFixtures {
    private const val INLINE_REPLIES = 2

    fun commentId(
        page: Int,
        index: Int,
    ): Int = 10_000 + page * 100 + index

    fun commentText(id: Int): String = "Comment $id"

    fun replyId(
        commentId: Int,
        index: Int,
    ): Int = commentId * 100 + index

    fun replyText(id: Int): String = "Reply $id"

    /** Page [page] of [linkId]'s top-level comments, under the sample's pagination block. */
    fun commentsPage(
        sample: JsonObject,
        linkId: Int,
        page: Int,
    ): String {
        val template = sample.items().first()
        val count = sample.pageSize(page)
        val inlineReply = template.getValue("comments").jsonObject.getValue("items").jsonArray.first().jsonObject
        val comments = (1..count).map { index ->
            val id = commentId(page, index)
            template.withoutImages().with(
                "id" to JsonPrimitive(id),
                "content" to JsonPrimitive(commentText(id)),
                "parent" to template.getValue("parent").jsonObject.withoutImages().with("id" to JsonPrimitive(linkId)),
                "comments" to template.getValue("comments").jsonObject.with(
                    "items" to JsonArray((1..INLINE_REPLIES).map { reply(inlineReply, linkId, id, it) }),
                ),
            )
        }
        return sample.with("data" to JsonArray(comments)).toString()
    }

    /** Page [page] of the replies to comment [commentId], under the sample's pagination block. */
    fun repliesPage(
        sample: JsonObject,
        linkId: Int,
        commentId: Int,
        page: Int,
    ): String {
        val perPage = sample.pagination().getValue("per_page").jsonPrimitive.int
        val replies = (1..sample.pageSize(page)).map { index ->
            reply(sample.items().first(), linkId, commentId, (page - 1) * perPage + index)
        }
        return sample.with("data" to JsonArray(replies)).toString()
    }

    private fun reply(
        template: JsonObject,
        linkId: Int,
        commentId: Int,
        index: Int,
    ): JsonObject {
        val id = replyId(commentId, index)
        val parent = template.getValue("parent").jsonObject.withoutImages()
        return template.withoutImages().with(
            "id" to JsonPrimitive(id),
            "content" to JsonPrimitive(replyText(id)),
            "parent_id" to JsonPrimitive(commentId),
            "parent" to parent.with(
                "id" to JsonPrimitive(commentId),
                "link" to parent.getValue("link").jsonObject.with("id" to JsonPrimitive(linkId)),
            ),
        )
    }

    private fun JsonObject.items(): List<JsonObject> = getValue("data").jsonArray.map { it.jsonObject }

    private fun JsonObject.pagination(): JsonObject = getValue("pagination").jsonObject

    private fun JsonObject.pageSize(page: Int): Int {
        val perPage = pagination().getValue("per_page").jsonPrimitive.int
        val total = pagination().getValue("total").jsonPrimitive.int
        return (total - (page - 1) * perPage).coerceIn(0, perPage)
    }
}
