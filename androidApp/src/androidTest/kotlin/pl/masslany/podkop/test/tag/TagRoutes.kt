package pl.masslany.podkop.test.tag

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import pl.masslany.podkop.test.fixtures.ResourceFixtures
import pl.masslany.podkop.test.fixtures.with
import pl.masslany.podkop.test.fixtures.withoutImages
import pl.masslany.podkop.test.support.MockApiServer

object TagRoutes {
    const val TAGS_PATH = "/api/v3/tags"
    const val STREAM_ITEMS = 3

    /** Sentinel text of the [index]th entry or link in a tag's stream. */
    fun streamItemText(
        type: String,
        index: Int,
    ): String = "Tag $type $index"

    /**
     * A tag as a signed-in user opens it on a content [type] (`all`, `entry` or `link`): the tag's
     * details and its stream, sorted by `all`. Signed-in streams are cursor-paged; this one ends
     * on its first page, and holds entries unless it shows only links.
     */
    fun MockApiServer.signedInTagStream(
        tagName: String,
        type: String,
    ) {
        val details = sample("tag-user")
        getJson(
            path = "$TAGS_PATH/$tagName",
            body = details.with(
                "data" to details.getValue("data").jsonObject.withoutImages().with("name" to JsonPrimitive(tagName)),
            ).toString(),
        )
        getJson(
            path = "$TAGS_PATH/$tagName/stream",
            query = mapOf("sort" to "all", "type" to type),
            body = ResourceFixtures.lastCursorPage(
                sample = sample("tags-stream-user-page-1"),
                resource = if (type == "link") "link" else "entry",
                count = STREAM_ITEMS,
                firstId = 7001,
                text = { index -> streamItemText(type, index) },
            ),
        )
    }
}
