package pl.masslany.podkop.features.resources

import kotlinx.coroutines.flow.first
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.embed.StreamableEmbedState
import pl.masslany.podkop.common.settings.AppSettings

/**
 * Inline Streamable playback shared by the screens that own embed state. They decide where the
 * resulting [StreamableEmbedState] is stored; this only answers whether to play inline and what to play.
 */
class StreamableEmbedPlayback(
    private val appSettings: AppSettings,
    private val streamableVideoRepository: StreamableVideoRepository,
    private val logger: AppLogger,
) {
    suspend fun isInlinePlaybackEnabled(): Boolean = appSettings.playVideosInline.first()

    /** Resolves a fresh signed MP4 into [StreamableEmbedState.Playing], or [StreamableEmbedState.Error]. */
    suspend fun resolve(url: String): StreamableEmbedState =
        streamableVideoRepository.getVideo(url).fold(
            onSuccess = { video ->
                StreamableEmbedState.Playing(
                    mp4Url = video.mp4Url,
                    aspectRatio = video.aspectRatio ?: DefaultStreamableAspectRatio,
                )
            },
            onFailure = { error ->
                logger.error(message = "Streamable video resolve failed for url=$url", throwable = error)
                StreamableEmbedState.Error
            },
        )
}

private const val DefaultStreamableAspectRatio = 16f / 9f
