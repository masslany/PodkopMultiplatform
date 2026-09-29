package pl.masslany.podkop.business.embeds.data.api

import kotlinx.serialization.json.JsonObject

interface StreamableVideoDataSource {
    suspend fun getVideo(shortcode: String): Result<JsonObject>
}
