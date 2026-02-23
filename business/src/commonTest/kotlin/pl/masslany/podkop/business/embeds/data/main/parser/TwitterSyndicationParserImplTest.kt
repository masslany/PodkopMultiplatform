package pl.masslany.podkop.business.embeds.data.main.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TwitterSyndicationParserImplTest {
    private val sut = TwitterSyndicationParserImpl()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses classic payload with counters and photo`() {
        val payload = parseJson(
            """
            {
              "text": "Hello &amp; world",
              "reply_count": 2,
              "retweet_count": 3,
              "favorite_count": 4,
              "photos": [{ "url": "https://img.test/p.jpg", "width": 800, "height": 400 }],
              "user": {
                "name": "Alice",
                "screen_name": "alice_dev",
                "profile_image_url_https": "https://img.test/a.jpg"
              }
            }
            """.trimIndent(),
        )

        val result = sut.parseTweetPreview(payload)

        assertNotNull(result)
        assertEquals("Alice", result.authorName)
        assertEquals("alice_dev", result.authorHandle)
        assertEquals("https://img.test/a.jpg", result.avatarUrl)
        assertEquals("Hello & world", result.text)
        assertEquals(2, result.replyCount)
        assertEquals(3, result.retweetCount)
        assertEquals(4, result.likeCount)
        assertEquals("https://img.test/p.jpg", result.mediaThumbnailUrl)
        assertEquals(2f, result.mediaAspectRatio)
    }

    @Test
    fun `parses note_tweet text and defaults counters`() {
        val payload = parseJson(
            """
            {
              "conversation_count": 7,
              "note_tweet": {
                "note_tweet_results": {
                  "result": {
                    "text": "Long &lt;tweet&gt;"
                  }
                }
              },
              "user": {
                "name": "Bob",
                "screen_name": "@bob",
                "profile_image_url_https": null
              }
            }
            """.trimIndent(),
        )

        val result = sut.parseTweetPreview(payload)

        assertNotNull(result)
        assertEquals("Bob", result.authorName)
        assertEquals("bob", result.authorHandle)
        assertEquals("Long <tweet>", result.text)
        assertEquals(7, result.replyCount)
        assertEquals(0, result.retweetCount)
        assertEquals(0, result.likeCount)
        assertNull(result.mediaThumbnailUrl)
        assertNull(result.mediaAspectRatio)
    }

    @Test
    fun `returns null when required fields missing`() {
        val missingUser = parseJson("""{ "text": "x" }""")
        val missingText = parseJson(
            """
            { "user": { "screen_name": "abc", "name": "A" } }
            """.trimIndent(),
        )

        assertNull(sut.parseTweetPreview(missingUser))
        assertNull(sut.parseTweetPreview(missingText))
    }

    private fun parseJson(raw: String) = json.parseToJsonElement(raw).jsonObject
}
