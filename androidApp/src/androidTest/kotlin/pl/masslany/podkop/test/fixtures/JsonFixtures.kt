package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun JsonObject.with(vararg fields: Pair<String, JsonElement>): JsonObject = JsonObject(this + fields)

/** Readable, but in the shape of a real id or cursor: [length] letters and digits. */
internal fun token(
    seed: String,
    length: Int,
): String = seed.padEnd(length, 'x').take(length)

/**
 * Swaps images for the values the API itself serves when there are none - `""` avatars and
 * backgrounds, a `null` photo and no `photos` - which also keeps the tests from loading images.
 */
internal fun JsonObject.withoutImages(): JsonObject =
    JsonObject(
        mapValues { (key, value) ->
            when (key) {
                "avatar", "background" -> JsonPrimitive("")
                "photo" -> JsonNull
                "photos" -> JsonArray(emptyList())
                else -> value.withoutImages()
            }
        },
    )

private fun JsonElement.withoutImages(): JsonElement =
    when (this) {
        is JsonObject -> withoutImages()
        is JsonArray -> JsonArray(map { it.withoutImages() })
        else -> this
    }
