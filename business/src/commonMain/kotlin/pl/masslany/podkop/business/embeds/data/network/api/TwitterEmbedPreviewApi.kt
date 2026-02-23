package pl.masslany.podkop.business.embeds.data.network.api

import kotlinx.serialization.json.JsonObject

interface TwitterEmbedPreviewApi {
    suspend fun getTweetResult(
        tweetId: Long,
        token: String?,
    ): Result<JsonObject>
}
