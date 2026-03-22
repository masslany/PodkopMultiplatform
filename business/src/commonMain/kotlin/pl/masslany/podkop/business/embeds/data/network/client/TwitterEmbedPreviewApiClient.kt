package pl.masslany.podkop.business.embeds.data.network.client

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.embeds.data.network.api.TwitterEmbedPreviewApi
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.REQUEST_HEADER_SKIP_AUTH
import pl.masslany.podkop.common.network.models.request.Request

class TwitterEmbedPreviewApiClient(
    private val apiClient: ApiClient,
) : TwitterEmbedPreviewApi {

    override suspend fun getTweetResult(
        tweetId: Long,
        token: String?,
    ): Result<JsonObject> {
        val queryParameters = buildMap {
            put("id", tweetId.toString())
            token?.let { put("token", it) }
        }

        val request = Request<JsonObject>(
            method = Request.HttpMethod.GET,
            path = TwitterEmbedEndpointUrl,
            headers = mapOf(REQUEST_HEADER_SKIP_AUTH to "true"),
            queryParameters = queryParameters,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}

private const val TwitterEmbedEndpointUrl = "https://cdn.syndication.twimg.com/tweet-result"
