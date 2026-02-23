package pl.masslany.podkop.business.embeds.data.api

import kotlinx.serialization.json.JsonObject

interface TwitterEmbedPreviewDataSource {
    suspend fun getTweet(
        tweetId: Long,
        token: String?,
    ): Result<JsonObject>
}
