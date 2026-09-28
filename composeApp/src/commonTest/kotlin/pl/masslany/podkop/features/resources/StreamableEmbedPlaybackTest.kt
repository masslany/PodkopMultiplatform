package pl.masslany.podkop.features.resources

import kotlinx.coroutines.runBlocking
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository
import pl.masslany.podkop.business.embeds.domain.models.StreamableVideo
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.StreamableEmbedState
import pl.masslany.podkop.common.preview.FakeAppSettings
import pl.masslany.podkop.testsupport.fakes.FakeAppLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StreamableEmbedPlaybackTest {

    @Test
    fun `inline playback follows the setting`() = runBlocking {
        assertFalse(createSut(playVideosInline = false).isInlinePlaybackEnabled())
        assertTrue(createSut(playVideosInline = true).isInlinePlaybackEnabled())
    }

    @Test
    fun `resolved video plays with its aspect ratio`() = runBlocking {
        val sut = createSut(result = Result.success(video(width = 800, height = 400)))

        assertEquals(StreamableEmbedState.Playing(mp4Url = "https://cdn/v.mp4", aspectRatio = 2f), sut.resolve(Url))
    }

    @Test
    fun `video without dimensions falls back to 16 by 9`() = runBlocking {
        val sut = createSut(result = Result.success(video(width = null, height = null)))

        assertEquals(16f / 9f, (sut.resolve(Url) as StreamableEmbedState.Playing).aspectRatio)
    }

    @Test
    fun `failed resolve becomes error and is logged`() = runBlocking {
        val logger = FakeAppLogger()
        val sut = createSut(result = Result.failure(IllegalStateException("404")), logger = logger)

        assertEquals(StreamableEmbedState.Error, sut.resolve(Url))
        assertEquals(1, logger.errorMessages.size)
    }

    @Test
    fun `streamable state updates only the matching streamable embed`() {
        val embed = EmbedContentState(
            key = "k",
            type = EmbedContentType.Streamable,
            url = Url,
            thumbnailUrl = "",
            streamableState = StreamableEmbedState.Preview,
        )
        val tweet = embed.copy(type = EmbedContentType.Twitter, streamableState = null)

        assertEquals(
            StreamableEmbedState.Loading,
            embed.updateStreamableEmbedStateIfMatches("k", StreamableEmbedState.Loading)?.streamableState,
        )
        assertSame(embed, embed.updateStreamableEmbedStateIfMatches("other", StreamableEmbedState.Loading))
        assertSame(tweet, tweet.updateStreamableEmbedStateIfMatches("k", StreamableEmbedState.Loading))
        assertNull(null.updateStreamableEmbedStateIfMatches("k", StreamableEmbedState.Loading))
    }

    private fun createSut(
        playVideosInline: Boolean = true,
        result: Result<StreamableVideo> = Result.success(video(width = 16, height = 9)),
        logger: FakeAppLogger = FakeAppLogger(),
    ) = StreamableEmbedPlayback(
        appSettings = FakeAppSettings(playVideosInlineInitial = playVideosInline),
        streamableVideoRepository = object : StreamableVideoRepository {
            override suspend fun getVideo(url: String): Result<StreamableVideo> = result
        },
        logger = logger,
    )

    private fun video(width: Int?, height: Int?) = StreamableVideo(
        shortcode = "moo",
        mp4Url = "https://cdn/v.mp4",
        width = width,
        height = height,
        thumbnailUrl = null,
    )
}

private const val Url = "https://streamable.com/moo"
