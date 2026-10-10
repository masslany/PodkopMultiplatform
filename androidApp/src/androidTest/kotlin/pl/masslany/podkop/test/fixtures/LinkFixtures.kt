package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds homepage responses from sanitized samples of real website responses (captured 2026-10-09).
 * A sample fixes each item's shape and the pagination block, and these builders only change values,
 * so generated pages stay true to what the API serves:
 *
 * - Every guest page holds `per_page` links plus a promoted link on top (`published_at` is null)
 *   and three entries at positions 5, 11 and 17; the promoted link and entries repeat on each page.
 * - Signed-in users get cursor pages: no `per_page` or `total`, only `next` and `prev`. The first
 *   page holds the promoted link and entries too, later pages only links; pages hold 40 links.
 * - Some links are marked `recommended`, here the fourth link of each page.
 */
object LinkFixtures {
    const val PROMOTED_LINK_ID = 9000
    const val PROMOTED_LINK_TITLE = "Promoted link"
    const val SIGNED_IN_LINKS_PER_PAGE = 40
    val entryIds = listOf(9001, 9002, 9003)

    private val entryPositions = listOf(5, 11, 17)
    private const val RECOMMENDED_LINK_INDEX = 4

    fun linkId(
        page: Int,
        index: Int,
    ): Int = page * 100 + index

    fun linkTitle(
        page: Int,
        index: Int,
    ): String = "Link $page-${index.toString().padStart(2, '0')}"

    /** The cursor the API hands out for requesting [page] (2 or later) of a signed-in feed. */
    fun pageCursor(
        sample: JsonObject,
        page: Int,
    ): String = token("Page${page}Cursor", length = sample.pagination().getValue("next").jsonPrimitive.content.length)

    /**
     * A numbered page of plain links under the sample's pagination block, as profile tabs serve
     * them. [links] can be fewer than `per_page`: the API returns short pages before the last one.
     */
    fun numberedLinkPage(
        sample: JsonObject,
        page: Int,
        links: Int,
    ): String = sample.with("data" to JsonArray(page(sample, page, links, promoted = false))).toString()

    /** A numbered page as guests get it, under the guest sample's pagination block. */
    fun numberedPage(
        sample: JsonObject,
        page: Int,
    ): String {
        val perPage = sample.pagination().getValue("per_page").jsonPrimitive.int
        return sample.with("data" to JsonArray(page(sample, page, links = perPage, promoted = true))).toString()
    }

    /**
     * A cursor page as signed-in users get it. [firstPage] and [laterPage] are samples of a first
     * and a later page, whose pagination blocks differ: the first page has no `prev` cursor.
     */
    fun cursorPage(
        firstPage: JsonObject,
        laterPage: JsonObject,
        page: Int,
    ): String {
        val sample = if (page == 1) firstPage else laterPage
        val items = page(firstPage, page, links = SIGNED_IN_LINKS_PER_PAGE, promoted = page == 1)
        val pagination = JsonObject(
            sample.pagination().mapValues { (key, value) ->
                when {
                    value is JsonNull -> value
                    key == "next" -> JsonPrimitive(pageCursor(firstPage, page + 1))
                    else -> JsonPrimitive(token("${key.replaceFirstChar(Char::uppercase)}${page}Cursor", value.jsonPrimitive.content.length))
                }
            },
        )
        return sample.with("data" to JsonArray(items), "pagination" to pagination).toString()
    }

    private fun page(
        sample: JsonObject,
        page: Int,
        links: Int,
        promoted: Boolean,
    ): List<JsonObject> {
        val items = sample.getValue("data").jsonArray.map { it.jsonObject }
        val plain = items.first { it.isLink() && !it.isPromoted() && "recommended" !in it }
        val recommended = items.firstOrNull { it.isLink() && !it.isPromoted() && "recommended" in it } ?: plain
        val feed = (1..links).mapTo(mutableListOf()) { index ->
            link(
                template = if (index == RECOMMENDED_LINK_INDEX) recommended else plain,
                id = linkId(page, index),
                title = linkTitle(page, index),
            )
        }
        if (promoted) {
            val entry = items.first { it.getValue("resource").jsonPrimitive.content == "entry" }
            feed.add(0, link(items.first { it.isLink() && it.isPromoted() }, PROMOTED_LINK_ID, PROMOTED_LINK_TITLE))
            entryPositions.zip(entryIds).forEach { (position, id) -> feed.add(position, entry(entry, id)) }
        }
        return feed
    }

    private fun link(
        template: JsonObject,
        id: Int,
        title: String,
    ): JsonObject {
        val source = template.getValue("source").jsonObject
        return template.withoutImages().with(
            "id" to JsonPrimitive(id),
            "title" to JsonPrimitive(title),
            "slug" to JsonPrimitive("link-$id"),
            // Like the API, where a link's source usually carries the link id as type_id.
            "source" to source.with("type_id" to JsonPrimitive(id)),
        )
    }

    private fun entry(
        template: JsonObject,
        id: Int,
    ): JsonObject =
        template.withoutImages().with(
            "id" to JsonPrimitive(id),
            "slug" to JsonPrimitive("entry-$id"),
            "content" to JsonPrimitive("Entry $id"),
        )
}

private fun JsonObject.pagination(): JsonObject = getValue("pagination").jsonObject

private fun JsonObject.isLink(): Boolean = getValue("resource").jsonPrimitive.content == "link"

private fun JsonObject.isPromoted(): Boolean = get("published_at") is JsonNull
