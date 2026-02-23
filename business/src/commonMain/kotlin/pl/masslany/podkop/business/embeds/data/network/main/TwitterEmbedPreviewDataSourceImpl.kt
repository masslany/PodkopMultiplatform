package pl.masslany.podkop.business.embeds.data.network.main

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.embeds.data.api.TwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.embeds.data.network.api.TwitterEmbedPreviewApi

class TwitterEmbedPreviewDataSourceImpl(
    private val api: TwitterEmbedPreviewApi,
) : TwitterEmbedPreviewDataSource {

    override suspend fun getTweet(
        tweetId: Long,
        token: String?,
    ): Result<JsonObject> = api.getTweetResult(
        tweetId = tweetId,
        token = token,
    )
}
