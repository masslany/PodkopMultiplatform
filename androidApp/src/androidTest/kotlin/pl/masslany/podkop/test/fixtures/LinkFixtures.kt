package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds link responses from a sanitized sample of a real website response. The sample fixes the
 * shape - every field, null and pagination key the backend sends - and these builders only change
 * values, so generated pages stay true to what the API serves.
 */
object LinkFixtures {
    fun linkId(
        page: Int,
        index: Int,
    ): Int = page * 100 + index

    fun linkTitle(
        page: Int,
        index: Int,
    ): String = "Link $page-${index.toString().padStart(2, '0')}"

    /** A numbered page as guests get it: `per_page` links under the sample's pagination block. */
    fun numberedPage(
        sample: JsonObject,
        page: Int,
    ): String {
        val template = sample.getValue("data").jsonArray.first().jsonObject
        val perPage = sample.getValue("pagination").jsonObject.getValue("per_page").jsonPrimitive.int
        val links = (1..perPage).map { index ->
            link(
                template = template,
                id = linkId(page, index),
                title = linkTitle(page, index),
            )
        }
        return sample.with("data" to JsonArray(links)).toString()
    }

    private fun link(
        template: JsonObject,
        id: Int,
        title: String,
    ): JsonObject {
        val source = template.getValue("source").jsonObject
        val author = template.getValue("author").jsonObject
        val media = template.getValue("media").jsonObject
        return template.with(
            "id" to JsonPrimitive(id),
            "title" to JsonPrimitive(title),
            "slug" to JsonPrimitive("link-$id"),
            // Like the API, where a link's source usually carries the link id as type_id.
            "source" to source.with("type_id" to JsonPrimitive(id)),
            // The API serves authors without an avatar and links without a photo as "" and null, which
            // also keeps the tests from loading images off the network.
            "author" to author.with("avatar" to JsonPrimitive("")),
            "media" to media.with("photo" to JsonNull),
        )
    }
}

private fun JsonObject.with(vararg fields: Pair<String, JsonElement>): JsonObject = JsonObject(this + fields)
