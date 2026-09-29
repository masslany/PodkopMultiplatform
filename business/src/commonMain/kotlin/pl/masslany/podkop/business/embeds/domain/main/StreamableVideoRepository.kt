package pl.masslany.podkop.business.embeds.domain.main

import pl.masslany.podkop.business.embeds.domain.models.StreamableVideo

interface StreamableVideoRepository {
    /**
     * Resolves a playable MP4 for a Streamable page url. The returned [StreamableVideo.mp4Url] is
     * signed and expires after a few days, so resolve it right before playback instead of caching.
     */
    suspend fun getVideo(url: String): Result<StreamableVideo>
}
