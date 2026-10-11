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

    /** Upvotes of the plain links in feeds built from [sample]; they share the sample's count. */
    fun upvotes(sample: JsonObject): Int =
        plainLink(sample).getValue("votes").jsonObject.getValue("up").jsonPrimitive.int

    /**
     * A link as `links/{id}` serves it, from the details sample, with [title] and, once the user
     * upvoted it, `voted: 1` and [upvotes].
     */
    fun details(
        sample: JsonObject,
        id: Int,
        title: String,
        upvotes: Int? = null,
    ): String {
        val data = sample.getValue("data").jsonObject.withoutImages()
        val votes = data.getValue("votes").jsonObject
        val vote = upvotes?.let { up ->
            arrayOf(
                "voted" to JsonPrimitive(1),
                "votes" to votes.with(
                    "up" to JsonPrimitive(up),
                    "count" to JsonPrimitive(up - votes.getValue("down").jsonPrimitive.int),
                ),
            )
        }.orEmpty()
        return sample.with(
            "data" to data.with("id" to JsonPrimitive(id), "title" to JsonPrimitive(title), *vote),
        ).toString()
    }

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
        sources: Map<Int, String> = emptyMap(),
    ): String {
        val sample = if (page == 1) firstPage else laterPage
        val items = page(firstPage, page, links = SIGNED_IN_LINKS_PER_PAGE, promoted = page == 1, sources = sources)
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
        sources: Map<Int, String> = emptyMap(),
    ): List<JsonObject> {
        val items = sample.getValue("data").jsonArray.map { it.jsonObject }
        val plain = plainLink(sample)
        val recommended = items.firstOrNull { it.isLink() && !it.isPromoted() && "recommended" in it } ?: plain
        val feed = (1..links).mapTo(mutableListOf()) { index ->
            link(
                template = if (index == RECOMMENDED_LINK_INDEX) recommended else plain,
                id = linkId(page, index),
                title = linkTitle(page, index),
                sourceUrl = sources[linkId(page, index)],
            )
        }
        if (promoted) {
            val entry = items.first { it.getValue("resource").jsonPrimitive.content == "entry" }
            feed.add(0, link(items.first { it.isLink() && it.isPromoted() }, PROMOTED_LINK_ID, PROMOTED_LINK_TITLE))
            entryPositions.zip(entryIds).forEach { (position, id) -> feed.add(position, entry(entry, id)) }
        }
        return feed
    }

    /** A link's source is labelled with its URL's host, e.g. `wykop.pl` for a link to the website. */
    fun sourceLabel(url: String): String = url.substringAfter("://").substringBefore('/')

    private fun link(
        template: JsonObject,
        id: Int,
        title: String,
        sourceUrl: String? = null,
    ): JsonObject {
        // Like the API, where a link's source usually carries the link id as type_id.
        val source = template.getValue("source").jsonObject.with("type_id" to JsonPrimitive(id))
        return template.withoutImages().with(
            "id" to JsonPrimitive(id),
            "title" to JsonPrimitive(title),
            "slug" to JsonPrimitive("link-$id"),
            "source" to if (sourceUrl == null) {
                source
            } else {
                source.with("url" to JsonPrimitive(sourceUrl), "label" to JsonPrimitive(sourceLabel(sourceUrl)))
            },
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

private fun plainLink(sample: JsonObject): JsonObject =
    sample.getValue("data").jsonArray
        .map { it.jsonObject }
        .first { it.isLink() && !it.isPromoted() && "recommended" !in it }

private fun JsonObject.isLink(): Boolean = getValue("resource").jsonPrimitive.content == "link"

private fun JsonObject.isPromoted(): Boolean = get("published_at") is JsonNull
