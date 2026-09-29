package pl.masslany.podkop.business.embeds.data.network.client

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.embeds.data.network.api.StreamableVideoApi
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.REQUEST_HEADER_SKIP_AUTH
import pl.masslany.podkop.common.network.models.request.Request

class StreamableVideoApiClient(
    private val apiClient: ApiClient,
) : StreamableVideoApi {

    override suspend fun getVideoResult(shortcode: String): Result<JsonObject> {
        val request = Request<JsonObject>(
            method = Request.HttpMethod.GET,
            path = "$StreamableVideosEndpointUrl/$shortcode",
            headers = mapOf(REQUEST_HEADER_SKIP_AUTH to "true"),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}

private const val StreamableVideosEndpointUrl = "https://api.streamable.com/videos"
