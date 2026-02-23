package pl.masslany.podkop.business.embeds.data.main

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.serialization.json.buildJsonObject
import pl.masslany.podkop.business.embeds.data.api.TwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.embeds.data.main.parser.TwitterSyndicationParser
import pl.masslany.podkop.business.embeds.data.main.token.TwitterSyndicationTokenGenerator
import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeTwitterSyndicationParser
import pl.masslany.podkop.business.testsupport.fakes.RecordingTwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.testsupport.fakes.RecordingTwitterSyndicationParser
import pl.masslany.podkop.business.testsupport.fakes.RecordingTwitterSyndicationTokenGenerator
import pl.masslany.podkop.business.testsupport.fakes.TwitterEmbedPreviewDataSourceCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TwitterEmbedPreviewRepositoryImplTest {

    @Test
    fun `returns failure for invalid twitter url without hitting datasource`() = runBlocking {
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, _ -> Result.success(buildJsonObject { }) }
        val sut = createSut(
            dataSource = dataSource,
            parser = FakeTwitterSyndicationParser(successTweet()),
        )

        val result = sut.getTweet("https://example.com/not-a-tweet")

        assertTrue(result.isFailure)
        assertTrue(dataSource.calls.isEmpty())
    }

    @Test
    fun `uses generated token and maps parser result`() = runBlocking {
        val root = buildJsonObject { }
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, _ -> Result.success(root) }
        val parser = RecordingTwitterSyndicationParser(successTweet())
        val tokenGenerator = RecordingTwitterSyndicationTokenGenerator("abc-token")
        val sut = createSut(
            dataSource = dataSource,
            parser = parser,
            tokenGenerator = tokenGenerator,
        )

        val result = sut.getTweet("https://x.com/user/status/2025929093305217210")

        assertTrue(result.isSuccess)
        assertEquals(successTweet(), result.getOrNull())
        assertEquals(listOf(2025929093305217210L), tokenGenerator.calls)
        assertEquals(
            listOf(TwitterEmbedPreviewDataSourceCall(2025929093305217210L, "abc-token")),
            dataSource.calls,
        )
        assertEquals(listOf(root), parser.calls)
    }

    @Test
    fun `retries without token when token request fails`() = runBlocking {
        val firstError = IllegalStateException("token rejected")
        val fallbackRoot = buildJsonObject { }
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, token ->
            if (token != null) {
                Result.failure(firstError)
            } else {
                Result.success(fallbackRoot)
            }
        }
        val parser = RecordingTwitterSyndicationParser(successTweet())
        val sut = createSut(
            dataSource = dataSource,
            parser = parser,
            tokenGenerator = RecordingTwitterSyndicationTokenGenerator("token-1"),
        )

        val result = sut.getTweet("https://twitter.com/user/status/42")

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                TwitterEmbedPreviewDataSourceCall(42L, "token-1"),
                TwitterEmbedPreviewDataSourceCall(42L, null),
            ),
            dataSource.calls,
        )
        assertEquals(listOf(fallbackRoot), parser.calls)
    }

    @Test
    fun `returns failure when parser cannot map payload`() = runBlocking {
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, _ -> Result.success(buildJsonObject { }) }
        val sut = createSut(
            dataSource = dataSource,
            parser = FakeTwitterSyndicationParser(null),
        )

        val result = sut.getTweet("https://x.com/user/status/123")

        assertTrue(result.isFailure)
    }

    @Test
    fun `deduplicates in flight requests by url`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, _ ->
            gate.await()
            Result.success(buildJsonObject { })
        }
        val repository = createSut(
            dataSource = dataSource,
            parser = FakeTwitterSyndicationParser(successTweet()),
            tokenGenerator = RecordingTwitterSyndicationTokenGenerator("tok"),
        )
        val url = "https://x.com/user/status/77"

        val first = async { repository.getTweet(url) }
        val second = async { repository.getTweet(url) }

        while (dataSource.calls.isEmpty()) {
            yield()
        }

        assertEquals(1, dataSource.calls.size)

        gate.complete(Unit)
        val firstResult = first.await()
        val secondResult = second.await()

        assertTrue(firstResult.isSuccess)
        assertTrue(secondResult.isSuccess)
        assertEquals(firstResult.getOrNull(), secondResult.getOrNull())
        assertEquals(1, dataSource.calls.size)
    }

    @Test
    fun `does not cache failed deferred after completion`() = runBlocking {
        var invocation = 0
        val dataSource = RecordingTwitterEmbedPreviewDataSource { _, _ ->
            invocation++
            when (invocation) {
                1, 2, 3 -> Result.failure(IllegalStateException("boom-$invocation"))
                else -> Result.success(buildJsonObject { })
            }
        }
        val sut = createSut(
            dataSource = dataSource,
            parser = FakeTwitterSyndicationParser(successTweet()),
            tokenGenerator = RecordingTwitterSyndicationTokenGenerator("tok"),
        )
        val url = "https://x.com/user/status/88"

        val first = sut.getTweet(url)
        val second = sut.getTweet(url)

        assertTrue(first.isFailure)
        assertTrue(second.isSuccess)
        assertEquals(
            listOf(
                TwitterEmbedPreviewDataSourceCall(88L, "tok"),
                TwitterEmbedPreviewDataSourceCall(88L, null),
                TwitterEmbedPreviewDataSourceCall(88L, "tok"),
                TwitterEmbedPreviewDataSourceCall(88L, null),
            ),
            dataSource.calls,
        )
    }

    private fun createSut(
        dataSource: TwitterEmbedPreviewDataSource,
        parser: TwitterSyndicationParser,
        tokenGenerator: TwitterSyndicationTokenGenerator = RecordingTwitterSyndicationTokenGenerator("token"),
    ) = TwitterEmbedPreviewRepositoryImpl(
        twitterDataSource = dataSource,
        dispatcherProvider = FakeDispatcherProvider(),
        twitterSyndicationParser = parser,
        twitterSyndicationTokenGenerator = tokenGenerator,
    )

    private fun successTweet() = TwitterEmbedPreview(
        authorName = "Tester",
        authorHandle = "tester",
        avatarUrl = null,
        text = "Hello",
        replyCount = 1,
        retweetCount = 2,
        likeCount = 3,
        mediaThumbnailUrl = null,
        mediaAspectRatio = null,
    )
}
