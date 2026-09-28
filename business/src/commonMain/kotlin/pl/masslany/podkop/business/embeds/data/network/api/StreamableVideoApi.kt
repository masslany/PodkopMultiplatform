package pl.masslany.podkop.business.embeds.data.network.api

import kotlinx.serialization.json.JsonObject

interface StreamableVideoApi {
    suspend fun getVideoResult(shortcode: String): Result<JsonObject>
}
