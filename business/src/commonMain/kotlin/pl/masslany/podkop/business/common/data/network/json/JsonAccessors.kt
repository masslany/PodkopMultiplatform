package pl.masslany.podkop.business.common.data.network.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Small, defensive helpers for reading dynamic JSON payloads.
 *
 * These are intentionally generic and live outside feature-specific packages so repositories that
 * integrate with unstable/unofficial endpoints can reuse them without duplicating low-level parsing
 * glue in each implementation file.
 *
 * They do *not* represent domain mapping rules. They only provide safe extraction primitives.
 */
internal fun JsonObject.getString(key: String): String? =
    get(key)?.jsonPrimitive?.contentOrNull

internal fun JsonObject.getInt(key: String): Int? =
    get(key)?.jsonPrimitive?.intOrNull

internal fun JsonObject.getObject(key: String): JsonObject? =
    get(key)?.jsonObjectOrNull()

internal fun JsonObject.getArray(key: String): JsonArray? =
    get(key)?.jsonArrayOrNull()

internal fun JsonElement.jsonObjectOrNull(): JsonObject? = runCatching { jsonObject }.getOrNull()

internal fun JsonElement.jsonArrayOrNull(): JsonArray? = runCatching { jsonArray }.getOrNull()
