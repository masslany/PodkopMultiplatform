package pl.masslany.podkop.common.components.embed.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Plays [url] inline with platform controls (play/pause, seek, fullscreen) and starts playback right
 * away. Pauses when another inline player starts or the screen goes to the background; releases the
 * player when it leaves composition and, back in composition (e.g. scrolled into view again), comes
 * back paused where it was left. Calls [onError] when the stream fails.
 */
@Composable
internal expect fun InlineVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    onError: () -> Unit,
)

/**
 * Keeps a single inline video playing at a time across the whole app. [active] is the player last asked to
 * play, until it pauses or leaves composition.
 */
internal object InlineVideoPlayback {
    private val _active = MutableStateFlow<Any?>(null)
    val active: StateFlow<Any?> = _active.asStateFlow()

    fun claim(token: Any) {
        _active.value = token
    }

    fun release(token: Any) {
        _active.compareAndSet(token, null)
    }
}

// Very tall or very wide clips would take over the feed; the player letterboxes the rest.
internal fun Float.clampedVideoAspectRatio(): Float = coerceIn(MinVideoAspectRatio, MaxVideoAspectRatio)

internal const val DefaultVideoAspectRatio = 16f / 9f
private const val MinVideoAspectRatio = 4f / 5f
private const val MaxVideoAspectRatio = 21f / 9f
