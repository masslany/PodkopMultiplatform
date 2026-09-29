package pl.masslany.podkop.business.embeds.data.main

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import pl.masslany.podkop.business.embeds.domain.models.StreamableVideo
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.RecordingStreamableVideoDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StreamableVideoRepositoryImplTest {

    @Test
    fun `extracts shortcode from supported url shapes`() {
        assertEquals("moo", "https://streamable.com/moo".extractStreamableShortcode())
        assertEquals("moo", "http://www.streamable.com/moo/".extractStreamableShortcode())
        assertEquals("moo", "https://streamable.com/e/moo?autoplay=1".extractStreamableShortcode())
        assertEquals("moo", "https://streamable.com/o/moo".extractStreamableShortcode())
        assertEquals("AbC12", " https://streamable.com/AbC12#t=3 ".extractStreamableShortcode())
    }

    @Test
    fun `rejects non streamable urls`() {
        assertNull("https://example.com/moo".extractStreamableShortcode())
        assertNull("https://streamable.com/".extractStreamableShortcode())
        assertNull("https://notstreamable.com/moo".extractStreamableShortcode())
    }

    @Test
    fun `returns failure for invalid url without hitting datasource`() = runBlocking {
        val dataSource = RecordingStreamableVideoDataSource { Result.success(readyPayload()) }
        val sut = createSut(dataSource)

        val result = sut.getVideo("https://youtube.com/watch?v=1")

        assertTrue(result.isFailure)
        assertTrue(dataSource.calls.isEmpty())
    }

    @Test
    fun `maps ready video and normalizes protocol relative urls`() = runBlocking {
        val dataSource = RecordingStreamableVideoDataSource { Result.success(readyPayload()) }
        val sut = createSut(dataSource)

        val result = sut.getVideo("https://streamable.com/moo")

        assertEquals(listOf("moo"), dataSource.calls)
        assertEquals(
            StreamableVideo(
                shortcode = "moo",
                mp4Url = "https://cdn.streamable.com/video/moo.mp4?Expires=1",
                width = 852,
                height = 480,
                thumbnailUrl = "https://cdn.streamable.com/image/moo.jpg",
            ),
            result.getOrNull(),
        )
        assertEquals(852f / 480f, result.getOrNull()?.aspectRatio)
    }

    @Test
    fun `fails when video is still processing`() = runBlocking {
        val sut = createSut(
            RecordingStreamableVideoDataSource {
                Result.success(json("""{"status": 1, "files": {"mp4": {"url": "https://a/b.mp4"}}}"""))
            },
        )

        assertTrue(sut.getVideo("https://streamable.com/moo").isFailure)
    }

    @Test
    fun `fails when mp4 rendition is missing`() = runBlocking {
        val sut = createSut(
            RecordingStreamableVideoDataSource { Result.success(json("""{"status": 2, "files": {}}""")) },
        )

        assertTrue(sut.getVideo("https://streamable.com/moo").isFailure)
    }

    @Test
    fun `drops non positive dimensions`() = runBlocking {
        val sut = createSut(
            RecordingStreamableVideoDataSource {
                Result.success(
                    json("""{"status": 2, "files": {"mp4": {"url": "https://a/b.mp4", "width": 0, "height": 0}}}"""),
                )
            },
        )

        val video = sut.getVideo("https://streamable.com/moo").getOrNull()

        assertNull(video?.width)
        assertNull(video?.aspectRatio)
        assertNull(video?.thumbnailUrl)
    }

    @Test
    fun `propagates datasource failure`() = runBlocking {
        val error = IllegalStateException("404")
        val sut = createSut(RecordingStreamableVideoDataSource { Result.failure(error) })

        assertEquals(error, sut.getVideo("https://streamable.com/moo").exceptionOrNull())
    }

    private fun createSut(dataSource: RecordingStreamableVideoDataSource) = StreamableVideoRepositoryImpl(
        dataSource = dataSource,
        dispatcherProvider = FakeDispatcherProvider(),
    )

    private fun readyPayload(): JsonObject = json(
        """
        {
          "status": 2,
          "thumbnail_url": "//cdn.streamable.com/image/moo.jpg",
          "files": {
            "mp4": {"url": "https://cdn.streamable.com/video/moo.mp4?Expires=1", "width": 852, "height": 480}
          }
        }
        """,
    )

    private fun json(raw: String): JsonObject = Json.parseToJsonElement(raw).jsonObject
}
