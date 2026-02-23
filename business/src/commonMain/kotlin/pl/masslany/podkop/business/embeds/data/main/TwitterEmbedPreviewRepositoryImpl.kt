package pl.masslany.podkop.business.embeds.data.main

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.masslany.podkop.business.embeds.data.api.TwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.embeds.data.main.parser.TwitterSyndicationParser
import pl.masslany.podkop.business.embeds.data.main.token.TwitterSyndicationTokenGenerator
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

/**
 * Repository for loading tweet preview data for inline embeds.
 *
 * Implementation notes:
 * 1. This integrates with X/Twitter's public syndication endpoint (`cdn.syndication.twimg.com`),
 *    not the official API v2.
 * 2. The payload schema is effectively "best effort" and can change without notice.
 * 3. We intentionally parse from [JsonObject] instead of DTOs because:
 *    - the endpoint is unofficial / unstable,
 *    - we only need a small subset of fields,
 *    - fallback parsing (e.g. `text` vs `note_tweet...text`) is easier to express defensively.
 * 4. We attempt a request with a generated syndication token first, then retry without a token if
 *    the endpoint rejects it or changes behavior.
 * 5. In-flight requests are deduplicated per embed URL to avoid duplicate network calls when the
 *    same tweet preview is clicked multiple times in a list.
 */
internal class TwitterEmbedPreviewRepositoryImpl(
    private val twitterDataSource: TwitterEmbedPreviewDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val twitterSyndicationParser: TwitterSyndicationParser,
    private val twitterSyndicationTokenGenerator: TwitterSyndicationTokenGenerator,
) : TwitterEmbedPreviewRepository {

    private val inFlightMutex = Mutex()
    private val inFlightByUrl = mutableMapOf<String, Deferred<Result<TwitterEmbedPreview>>>()

    override suspend fun getTweet(url: String): Result<TwitterEmbedPreview> = coroutineScope {
        var isOwner = false
        val deferred = inFlightMutex.withLock {
            inFlightByUrl[url] ?: async(dispatcherProvider.io) {
                fetchTweetInternal(url)
            }.also {
                inFlightByUrl[url] = it
                isOwner = true
            }
        }

        try {
            deferred.await()
        } finally {
            if (isOwner) {
                inFlightMutex.withLock {
                    if (inFlightByUrl[url] === deferred) {
                        inFlightByUrl.remove(url)
                    }
                }
            }
        }
    }

    /**
     * Loads and maps a tweet preview from the syndication endpoint.
     *
     * The endpoint may accept a token parameter derived from tweet id. Because this is not a
     * stable/public contract, we also retry without the token as a compatibility fallback.
     */
    private suspend fun fetchTweetInternal(url: String): Result<TwitterEmbedPreview> {
        val tweetId = url.extractTwitterStatusId()
            ?: return Result.failure(IllegalArgumentException("Twitter status id not found in url=$url"))

        return twitterDataSource.getTweet(
            tweetId = tweetId,
            token = twitterSyndicationTokenGenerator.generate(tweetId),
        ).mapCatching { root ->
            twitterSyndicationParser.parseTweetPreview(root)
                ?: error("Twitter embed response missing required fields")
        }.recoverCatching {
            val fallback = twitterDataSource.getTweet(
                tweetId = tweetId,
                token = null,
            ).getOrThrow()

            twitterSyndicationParser.parseTweetPreview(fallback)
                ?: error("Twitter embed response missing required fields")
        }
    }
}

private val TwitterStatusIdRegex =
    Regex("""https?://(?:www\.)?(?:x|twitter)\.com/[^/]+/status(?:es)?/(\d+)""", RegexOption.IGNORE_CASE)

private fun String.extractTwitterStatusId(): Long? =
    TwitterStatusIdRegex.find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toLongOrNull()
