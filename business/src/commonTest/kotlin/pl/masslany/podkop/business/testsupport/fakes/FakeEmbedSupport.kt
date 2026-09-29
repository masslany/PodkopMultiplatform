package pl.masslany.podkop.business.testsupport.fakes

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.embeds.data.api.StreamableVideoDataSource
import pl.masslany.podkop.business.embeds.data.api.TwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.embeds.data.main.parser.TwitterSyndicationParser
import pl.masslany.podkop.business.embeds.data.main.token.TwitterSyndicationTokenGenerator
import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview

data class TwitterEmbedPreviewDataSourceCall(
    val tweetId: Long,
    val token: String?,
)

class RecordingTwitterEmbedPreviewDataSource(
    private val handler: suspend (tweetId: Long, token: String?) -> Result<JsonObject>,
) : TwitterEmbedPreviewDataSource {
    val calls = mutableListOf<TwitterEmbedPreviewDataSourceCall>()

    override suspend fun getTweet(tweetId: Long, token: String?): Result<JsonObject> {
        calls += TwitterEmbedPreviewDataSourceCall(tweetId, token)
        return handler(tweetId, token)
    }
}

class RecordingTwitterSyndicationParser(
    private val mapped: TwitterEmbedPreview?,
) : TwitterSyndicationParser {
    val calls = mutableListOf<JsonObject>()

    override fun parseTweetPreview(root: JsonObject): TwitterEmbedPreview? {
        calls += root
        return mapped
    }
}

class FakeTwitterSyndicationParser(
    private val mapped: TwitterEmbedPreview?,
) : TwitterSyndicationParser {
    override fun parseTweetPreview(root: JsonObject): TwitterEmbedPreview? = mapped
}

class RecordingTwitterSyndicationTokenGenerator(
    private val token: String,
) : TwitterSyndicationTokenGenerator {
    val calls = mutableListOf<Long>()

    override fun generate(tweetId: Long): String {
        calls += tweetId
        return token
    }
}

class RecordingStreamableVideoDataSource(
    private val handler: suspend (shortcode: String) -> Result<JsonObject>,
) : StreamableVideoDataSource {
    val calls = mutableListOf<String>()

    override suspend fun getVideo(shortcode: String): Result<JsonObject> {
        calls += shortcode
        return handler(shortcode)
    }
}
