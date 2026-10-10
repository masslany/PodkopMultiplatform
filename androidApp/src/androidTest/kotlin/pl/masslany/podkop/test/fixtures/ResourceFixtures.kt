package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Builds responses for any resource list or object from a sanitized sample, changing only values. */
object ResourceFixtures {
    /**
     * Page [page] of a numbered list: the sample's items cycled to fill `per_page`, or what is left
     * of `total`, with ids starting at [firstId] plus the page offset.
     */
    fun numberedPage(
        sample: JsonObject,
        page: Int,
        firstId: Int,
    ): String {
        val items = sample.getValue("data").jsonArray.map { it.jsonObject }
        val pagination = sample.getValue("pagination").jsonObject
        val perPage = pagination.getValue("per_page").jsonPrimitive.int
        val total = pagination.getValue("total").jsonPrimitive.int
        val offset = (page - 1) * perPage
        val count = (total - offset).coerceIn(0, perPage)
        val data = (0 until count).map { index ->
            items[index % items.size].withoutImages().with("id" to JsonPrimitive(firstId + offset + index))
        }
        return sample.with("data" to JsonArray(data)).toString()
    }

    /**
     * The last page of a cursor-paged stream, as signed-in feeds end it (`next` and `prev` null):
     * [count] items shaped like the sample's [resource] items (`entry` or `link`), with ids from
     * [firstId] and [text] as each entry's content or link's title.
     */
    fun lastCursorPage(
        sample: JsonObject,
        resource: String,
        count: Int,
        firstId: Int,
        text: (index: Int) -> String,
    ): String {
        val template = sample.getValue("data").jsonArray
            .map { it.jsonObject }
            .first { it.getValue("resource").jsonPrimitive.content == resource }
            .withoutImages()
        val textField = if (resource == "link") "title" else "content"
        val data = (1..count).map { index ->
            template.with(
                "id" to JsonPrimitive(firstId + index - 1),
                textField to JsonPrimitive(text(index)),
            )
        }
        val pagination = JsonObject(sample.getValue("pagination").jsonObject.mapValues { JsonNull })
        return sample.with("data" to JsonArray(data), "pagination" to pagination).toString()
    }

    /** A single-object response (`{"data": {...}}`) for the resource with [id], and [content] if given. */
    fun withId(
        sample: JsonObject,
        id: Int,
        content: String? = null,
    ): String {
        val data = sample.getValue("data").jsonObject.withoutImages().with("id" to JsonPrimitive(id))
        return sample.with("data" to if (content == null) data else data.with("content" to JsonPrimitive(content))).toString()
    }
}
