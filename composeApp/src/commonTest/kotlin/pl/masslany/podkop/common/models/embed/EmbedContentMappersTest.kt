package pl.masslany.podkop.common.models.embed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.business.common.domain.models.common.Embed

class EmbedContentMappersTest {

    @Test
    fun `text-only tweet without thumbnail keeps its preview`() {
        val state = Embed(key = "k", thumbnail = "", type = "twitter", url = "https://x.com/a/status/1")
            .toEmbedContentState()

        assertEquals(EmbedContentType.Twitter, state?.type)
        assertEquals(TwitterEmbedState.Preview, state?.twitterState)
    }

    @Test
    fun `streamable embed starts in preview and tweets carry no streamable state`() {
        val streamable = Embed(key = "k", thumbnail = "t", type = "streamable", url = "https://streamable.com/moo")
            .toEmbedContentState()
        val tweet = Embed(key = "k", thumbnail = "", type = "twitter", url = "https://x.com/a/status/1")
            .toEmbedContentState()

        assertEquals(StreamableEmbedState.Preview, streamable?.streamableState)
        assertNull(streamable?.twitterState)
        assertNull(tweet?.streamableState)
    }

    @Test
    fun `other embeds without thumbnail are dropped`() {
        val state = Embed(key = "k", thumbnail = "", type = "youtube", url = "https://youtube.com/watch?v=1")
            .toEmbedContentState()

        assertNull(state)
    }
}
