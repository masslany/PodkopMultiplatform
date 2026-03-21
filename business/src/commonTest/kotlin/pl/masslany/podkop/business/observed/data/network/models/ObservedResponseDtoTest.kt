package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.data.main.mapper.common.toDeleted
import pl.masslany.podkop.business.common.domain.models.common.Deleted

class ObservedResponseDtoTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `direct observed payload decodes into observed items`() {
        val actual = json.decodeFromString<ObservedResponseDto>(
            """
            {
              "data": [
                {
                  "id": 18,
                  "resource": "link",
                  "title": "Sample",
                  "slug": "sample",
                  "description": "desc",
                  "created_at": "2026-03-20 21:42:30",
                  "deleted": null,
                  "adult": false,
                  "archive": false,
                  "deletable": false,
                  "editable": false,
                  "favourite": false,
                  "hot": false,
                  "actions": {
                    "create": true,
                    "create_favourite": false,
                    "delete": false,
                    "delete_favourite": false,
                    "finish_ama": false,
                    "report": true,
                    "start_ama": false,
                    "undo_vote": false,
                    "update": false,
                    "vote_down": true,
                    "vote_up": true,
                    "vote": false
                  },
                  "author": {
                    "username": "tester",
                    "gender": "m",
                    "company": false,
                    "avatar": "",
                    "status": "active",
                    "color": "orange",
                    "verified": false,
                    "rank": { "position": null, "trend": 0 },
                    "blacklist": false,
                    "follow": false,
                    "note": false,
                    "online": false
                  },
                  "comments": { "count": 0, "hot": false, "items": [] },
                  "media": { "photo": null, "embed": null },
                  "tags": [],
                  "voted": 0,
                  "votes": { "up": 0, "down": 0, "count": 0 }
                }
              ],
              "pagination": { "prev": null, "next": "abc" }
            }
            """.trimIndent(),
        )

        assertEquals(1, actual.data.size)
        assertEquals("link", actual.data.single().type)
        assertEquals("link", actual.data.single().item.resource)
    }

    @Test
    fun `discussion wrapper payload decodes into observed items and tolerates boolean deleted`() {
        val actual = json.decodeFromString<ObservedResponseDto>(
            """
            {
              "data": [
                {
                  "type": "entry",
                  "object": {
                    "id": 77,
                    "slug": "wrapped-entry",
                    "content": "wrapped",
                    "created_at": "2026-03-20 21:42:49",
                    "deleted": false,
                    "adult": false,
                    "archive": false,
                    "deletable": false,
                    "editable": false,
                    "favourite": false,
                    "actions": {
                      "create": true,
                      "create_favourite": false,
                      "delete": false,
                      "delete_favourite": false,
                      "finish_ama": false,
                      "report": true,
                      "start_ama": false,
                      "undo_vote": false,
                      "update": false,
                      "vote_down": false,
                      "vote_up": true,
                      "vote": false
                    },
                    "author": {
                      "username": "tester",
                      "gender": "m",
                      "company": false,
                      "avatar": "",
                      "status": "active",
                      "color": "orange",
                      "verified": false,
                      "rank": { "position": null, "trend": 0 },
                      "blacklist": false,
                      "follow": false,
                      "note": false,
                      "online": false
                    },
                    "comments": { "count": 0, "items": [] },
                    "media": { "photo": null, "embed": null, "survey": null },
                    "tags": [],
                    "voted": 0,
                    "votes": { "up": 0, "down": 0, "users": [] }
                  },
                  "new_content_count": 41
                }
              ],
              "pagination": { "prev": null, "next": null }
            }
            """.trimIndent(),
        )

        val item = actual.data.single()

        assertEquals("entry", item.type)
        assertEquals("entry", item.item.resource)
        assertEquals(41, item.newContentCount)
        assertEquals(Deleted.None, item.item.deleted.toDeleted())
    }
}
