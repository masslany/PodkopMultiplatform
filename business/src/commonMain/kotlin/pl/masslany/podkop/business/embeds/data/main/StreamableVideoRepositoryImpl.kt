package pl.masslany.podkop.business.embeds.data.main

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pl.masslany.podkop.business.embeds.data.api.StreamableVideoDataSource
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository
import pl.masslany.podkop.business.embeds.domain.models.StreamableVideo
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

/**
 * Resolves Streamable page urls into directly playable MP4 files.
 *
 * Implementation notes:
 * 1. Uses the public, unauthenticated `api.streamable.com/videos/{shortcode}` endpoint.
 * 2. The payload is parsed defensively from [JsonObject] because we only need a handful of fields
 *    and the endpoint is not a versioned contract.
 * 3. A video is playable only once processing finished (`status == 2`) and an MP4 rendition exists.
 */
internal class StreamableVideoRepositoryImpl(
    private val dataSource: StreamableVideoDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : StreamableVideoRepository {

    override suspend fun getVideo(url: String): Result<StreamableVideo> {
        val shortcode = url.extractStreamableShortcode()
            ?: return Result.failure(IllegalArgumentException("Streamable shortcode not found in url=$url"))

        return withContext(dispatcherProvider.io) {
            dataSource.getVideo(shortcode).mapCatching { root ->
                root.toStreamableVideo(shortcode)
                    ?: error("Streamable video $shortcode is not playable yet")
            }
        }
    }
}

private fun JsonObject.toStreamableVideo(shortcode: String): StreamableVideo? {
    val status = this["status"]?.jsonPrimitive?.intOrNull
    if (status != StreamableStatusReady) return null

    val mp4 = this["files"]?.jsonObject?.get("mp4")?.jsonObject ?: return null
    val mp4Url = mp4["url"]?.jsonPrimitive?.contentOrNull?.toAbsoluteUrl() ?: return null

    return StreamableVideo(
        shortcode = shortcode,
        mp4Url = mp4Url,
        width = mp4["width"]?.jsonPrimitive?.intOrNull?.takeIf { it > 0 },
        height = mp4["height"]?.jsonPrimitive?.intOrNull?.takeIf { it > 0 },
        thumbnailUrl = this["thumbnail_url"]?.jsonPrimitive?.contentOrNull?.toAbsoluteUrl(),
    )
}

// Streamable returns protocol-relative urls (`//cdn...`) for some fields.
private fun String.toAbsoluteUrl(): String? = when {
    isBlank() -> null
    startsWith("//") -> "https:$this"
    else -> this
}

private const val StreamableStatusReady = 2

private val StreamableShortcodeRegex =
    Regex("""^https?://(?:www\.)?streamable\.com/(?:[eos]/)?([a-z0-9]+)(?:[/?#].*)?$""", RegexOption.IGNORE_CASE)

internal fun String.extractStreamableShortcode(): String? =
    StreamableShortcodeRegex.find(trim())?.groupValues?.getOrNull(1)
