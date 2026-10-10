package pl.masslany.podkop.test.fixtures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds notification responses from sanitized samples of real website responses (captured
 * 2026-10-09), changing only values:
 *
 * - A grouped list (`show_grouped=1`) holds one row per group (`show_as_group`, `group_count`),
 *   numbered by `per_page` and `total`.
 * - A group's own notifications (`notifications/groups/{id}`) come in numbered pages of 25, each
 *   carrying the group's id and count.
 */
object NotificationFixtures {
    /** Group ids are UUIDs. */
    fun groupId(index: Int): String = "00000000-0000-4000-8000-${index.toString().padStart(12, '0')}"

    fun memberId(
        sample: JsonObject,
        page: Int,
        index: Int,
    ): String = token("M${page}i${index.toString().padStart(2, '0')}", length = sample.idLength())

    /** A page of groups: the sample's rows, cycled up to its `total`, each with its own ids. */
    fun groupedPage(sample: JsonObject): String {
        val rows = sample.rows()
        val total = sample.pagination().getValue("total").jsonPrimitive.int
        val groups = (1..total).map { index ->
            rows[(index - 1) % rows.size].withoutImages().with(
                "id" to JsonPrimitive(token("G${index}row", length = sample.idLength())),
                "group_id" to JsonPrimitive(groupId(index)),
            )
        }
        return sample.with("data" to JsonArray(groups)).toString()
    }

    /**
     * Page [page] of the notifications inside group [groupId], under the sample's pagination block.
     * They share the group's [read] state: a group reads as read once its notifications are.
     */
    fun membersPage(
        sample: JsonObject,
        groupId: String,
        page: Int,
        read: Boolean,
    ): String {
        val pagination = sample.pagination()
        val perPage = pagination.getValue("per_page").jsonPrimitive.int
        val total = pagination.getValue("total").jsonPrimitive.int
        val count = (total - (page - 1) * perPage).coerceIn(0, perPage)
        val members = (1..count).map { index ->
            sample.rows().first().withoutImages().with(
                "id" to JsonPrimitive(memberId(sample, page, index)),
                "group_id" to JsonPrimitive(groupId),
                "read" to JsonPrimitive(if (read) 1 else 0),
            )
        }
        return sample.with("data" to JsonArray(members)).toString()
    }

    private fun JsonObject.rows(): List<JsonObject> = getValue("data").jsonArray.map { it.jsonObject }

    private fun JsonObject.pagination(): JsonObject = getValue("pagination").jsonObject

    private fun JsonObject.idLength(): Int = rows().first().getValue("id").jsonPrimitive.content.length
}
