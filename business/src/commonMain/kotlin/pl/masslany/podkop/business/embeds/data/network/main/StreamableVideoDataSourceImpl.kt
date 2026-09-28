package pl.masslany.podkop.business.embeds.data.network.main

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.embeds.data.api.StreamableVideoDataSource
import pl.masslany.podkop.business.embeds.data.network.api.StreamableVideoApi

class StreamableVideoDataSourceImpl(
    private val api: StreamableVideoApi,
) : StreamableVideoDataSource {

    override suspend fun getVideo(shortcode: String): Result<JsonObject> = api.getVideoResult(shortcode)
}
