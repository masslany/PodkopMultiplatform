package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
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

    /** A single-object response (`{"data": {...}}`) for the resource with [id]. */
    fun withId(
        sample: JsonObject,
        id: Int,
    ): String = sample.with("data" to sample.getValue("data").jsonObject.withoutImages().with("id" to JsonPrimitive(id))).toString()
}
