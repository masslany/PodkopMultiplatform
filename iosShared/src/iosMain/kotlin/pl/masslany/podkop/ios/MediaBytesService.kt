package pl.masslany.podkop.ios

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURLCache
import platform.Foundation.NSURLRequestUseProtocolCachePolicy
import platform.Foundation.create
import pl.masslany.podkop.common.network.api.HttpStatusFailure

/** Raw image bytes for decoding in Swift; [mimeType] is the server's content type when known. */
class IOSMediaBytes(val data: NSData, val mimeType: String?)

/**
 * Downloads public media (photos, embed thumbnails, avatars, badges) for rendering in Swift.
 *
 * Uses its own unauthenticated client so API tokens are never sent to image hosts, accepts only
 * http(s) URLs, caps each response at [MAX_BYTES], and keeps a bounded on-disk HTTP cache that
 * follows the servers' cache headers, like Android's Coil disk cache.
 */
class MediaBytesService internal constructor(private val client: PodkopClient) {
    private val urlCache = NSURLCache(
        memoryCapacity = MEMORY_CACHE_BYTES.toULong(),
        diskCapacity = DISK_CACHE_BYTES.toULong(),
        diskPath = "podkop-media",
    )
    private val http = HttpClient(Darwin) {
        engine {
            configureSession {
                setURLCache(urlCache)
                setRequestCachePolicy(NSURLRequestUseProtocolCachePolicy)
                setHTTPMaximumConnectionsPerHost(6)
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        expectSuccess = false
    }

    fun load(url: String, completion: (IOSMediaBytes?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            val target = url.trim()
            require(target.startsWith("https://", ignoreCase = true) || target.startsWith("http://", ignoreCase = true)) {
                "invalid media url"
            }
            val response = http.get(target)
            if (!response.status.isSuccess()) throw MediaHttpFailure(response.status.value)
            val declared = response.contentLength()
            require(declared == null || declared in 1..MAX_BYTES) { "media too large" }
            val bytes = response.readRawBytes()
            require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES) { "media too large" }
            IOSMediaBytes(bytes.toNSData(), response.contentType()?.let { "${it.contentType}/${it.contentSubtype}" })
        }

    /** Removes cached media only; account data and settings are untouched. */
    fun clearCache() {
        urlCache.removeAllCachedResponses()
    }

    fun cachedBytes(): Long = urlCache.currentDiskUsage.toLong()

    internal fun close() {
        http.close()
    }

    private class MediaHttpFailure(override val statusCode: Int) : HttpStatusFailure, Exception("HTTP $statusCode")

    private companion object {
        const val MAX_BYTES = 20L * 1024 * 1024
        const val MEMORY_CACHE_BYTES = 8L * 1024 * 1024
        const val DISK_CACHE_BYTES = 150L * 1024 * 1024
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}
