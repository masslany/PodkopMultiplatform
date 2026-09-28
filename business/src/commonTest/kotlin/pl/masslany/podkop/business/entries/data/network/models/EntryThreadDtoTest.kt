package pl.masslany.podkop.business.entries.data.network.models

import kotlinx.serialization.json.Json
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.entries.data.main.mapper.toEntryThread
import pl.masslany.podkop.business.entries.data.main.mapper.toEntryThreadReplies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EntryThreadDtoTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `thread payload decodes nested replies`() {
        val actual = json.decodeFromString<EntryThreadResponseDto>(threadPayload)

        val topLevel = actual.data.replies!!.items.single()
        val reply = topLevel.replies!!.items.single()
        assertEquals(1, actual.data.nesting)
        assertEquals(78, actual.data.replies.count)
        assertEquals(10, topLevel.item.id)
        assertEquals(2, topLevel.nesting)
        assertEquals(9, topLevel.replies.count)
        assertEquals(11, reply.item.id)
        assertTrue(reply.host)
        assertEquals("entry_comment", reply.item.parent?.resource)
        assertTrue(reply.replies!!.items.isEmpty())
    }

    @Test
    fun `thread payload maps to a reply tree`() {
        val actual = json.decodeFromString<EntryThreadResponseDto>(threadPayload).data.toEntryThread()

        assertEquals(1, actual.entry.id)
        assertEquals(Resource.Entry, actual.entry.resource)
        assertEquals(78, actual.comments.totalCount)
        assertEquals(77, actual.comments.remainingCount)
        val topLevel = actual.comments.items.single()
        assertEquals(0, topLevel.depth)
        assertFalse(topLevel.isByEntryAuthor)
        assertEquals(10, topLevel.replyParentId)
        assertEquals(8, topLevel.replies.remainingCount)
        // Thread nodes only send `media.photos`, which falls back into the single photo slot.
        assertEquals("https://example.com/a.png", topLevel.comment.media?.photo?.url)
        val reply = topLevel.replies.items.single()
        assertEquals(Resource.EntryComment, reply.comment.resource)
        assertEquals(1, reply.depth)
        assertTrue(reply.isByEntryAuthor)
        assertEquals(11, reply.replyParentId)
    }

    @Test
    fun `nested replies address their entry as parent so comment actions hit the entry`() {
        val actual = json.decodeFromString<EntryThreadResponseDto>(threadPayload).data.toEntryThread()

        val reply = actual.comments.items.single().replies.items.single()
        assertEquals(1, reply.comment.parent?.id)
        assertEquals(1, reply.comment.parentId)
    }

    @Test
    fun `replies past the depth limit target the parent comment`() {
        val payload = """{"data":{"count":1,"total":1,"items":[${node(id = 70, nesting = 7, parentId = 60)}]}}"""

        val actual = json.decodeFromString<EntryThreadRepliesResponseDto>(payload).data
            .toEntryThreadReplies(entryId = 1, fallbackDepth = 1)

        val comment = actual.items.single()
        assertEquals(5, comment.depth)
        assertEquals(60, comment.replyParentId)
    }

    @Test
    fun `missing comments object maps to no replies`() {
        val payload = """{"data":${node(id = 5, nesting = null, parentId = null, withComments = false)}}"""

        val actual = json.decodeFromString<EntryThreadResponseDto>(payload).data

        assertNull(actual.replies)
        assertNull(actual.nesting)
        assertEquals(0, actual.toEntryThread().comments.totalCount)
    }

    @Test
    fun `thread node encodes back to the same payload shape`() {
        val decoded = json.decodeFromString<EntryThreadResponseDto>(threadPayload)

        val roundTrip = json.decodeFromString<EntryThreadResponseDto>(json.encodeToString(decoded))

        assertEquals(decoded, roundTrip)
    }

    private val threadPayload = """
        {"data": ${
        node(
            id = 1,
            nesting = 1,
            parentId = null,
            resource = "entry",
            count = 78,
            children = listOf(
                node(
                    id = 10,
                    nesting = 2,
                    parentId = 1,
                    parentResource = "entry",
                    count = 9,
                    photos = """[{"key":"k","label":"a","mime_type":"image/png","size":1,"url":"https://example.com/a.png","width":1,"height":1}]""",
                    children = listOf(node(id = 11, nesting = 3, parentId = 10, host = true)),
                ),
            ),
        )
    }}
    """.trimIndent()

    @Suppress("LongParameterList")
    private fun node(
        id: Int,
        nesting: Int?,
        parentId: Int?,
        resource: String = "entry_comment",
        parentResource: String = "entry_comment",
        host: Boolean = false,
        count: Int = 0,
        photos: String = "[]",
        children: List<String> = emptyList(),
        withComments: Boolean = true,
    ): String {
        val parent = parentId?.let {
            """{"resource":"$parentResource","id":$it,"slug":"p","author":$AUTHOR,"location":[{"filter":"oldest","page":1}]}"""
        } ?: "null"
        val comments = if (withComments) {
            ""","comments":{"count":$count,"total":$count,"items":[${children.joinToString(",")}]}"""
        } else {
            ""
        }
        val nestingField = nesting?.let { ""","nesting":$it""" }.orEmpty()
        return """
            {"id":$id,"slug":"s$id","author":$AUTHOR,"created_at":"2026-09-28 08:13:38","voted":0,
            "content":"c$id","media":{"photos":$photos,"embed":null},"adult":false,"tags":[],"favourite":false,
            "parent":$parent,"votes":{"up":1,"down":0},"editable":false,"deletable":false,"blacklist":false,
            "deleted":null,"resource":"$resource","archive":false,"host":$host$nestingField$comments}
        """.trimIndent()
    }

    private companion object {
        const val AUTHOR = """{"username":"tester","gender":null,"company":false,"avatar":"","status":"active",""" +
            """"color":"orange","verified":false,"rank":{"position":null,"trend":0},"blacklist":false,""" +
            """"follow":false,"note":false,"online":true}"""
    }
}
