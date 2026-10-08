package pl.masslany.podkop.common.components.embed.video

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
internal actual fun InlineVideoPlayer(
    url: String,
    modifier: Modifier,
    onError: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnError by rememberUpdatedState(onError)
    val token = remember { Any() }
    var isFullscreen by remember { mutableStateOf(false) }
    // Saved with the list item, so a clip scrolled out of view comes back paused where it was left.
    val player = rememberSaveable(url, saver = inlinePlayerSaver(context, url)) {
        inlinePlayer(context, url, playWhenReady = true, positionMs = 0L)
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            // Claims as soon as playback is asked for, not once frames flow, and lets go on pause, so a clip
            // paused elsewhere can't stop one that is still buffering.
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) InlineVideoPlayback.claim(token) else InlineVideoPlayback.release(token)
            }

            override fun onPlayerError(error: PlaybackException) {
                currentOnError()
            }
        }
        player.addListener(listener)
        if (player.playWhenReady) InlineVideoPlayback.claim(token)
        onDispose {
            player.removeListener(listener)
            player.release()
            InlineVideoPlayback.release(token)
        }
    }

    LaunchedEffect(player) {
        InlineVideoPlayback.active.collect { active ->
            if (active != null && active !== token) player.pause()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        player.pause()
    }

    // Only one PlayerView may render the player at a time, so the inline one lets go while fullscreen.
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                setShowNextButton(false)
                setShowPreviousButton(false)
                setShutterBackgroundColor(AndroidColor.BLACK)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setFullscreenButtonClickListener { isFullscreen = true }
            }
        },
        update = { view ->
            view.player = if (isFullscreen) null else player
            view.setFullscreenButtonState(false)
        },
    )

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShutterBackgroundColor(AndroidColor.BLACK)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setFullscreenButtonState(true)
                        setFullscreenButtonClickListener { isFullscreen = false }
                        this.player = player
                    }
                },
                onRelease = { view -> view.player = null },
            )
        }
    }
}

private fun inlinePlayer(context: Context, url: String, playWhenReady: Boolean, positionMs: Long): ExoPlayer =
    ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(url), positionMs)
        this.playWhenReady = playWhenReady
        prepare()
    }

// Restores only for the same signed url; a fresh one (a retry, a re-resolve) starts over and plays. A clip
// that was playing comes back paused too: it stopped when it left, and it shouldn't start sound unasked.
private fun inlinePlayerSaver(context: Context, url: String) = listSaver<ExoPlayer, Any>(
    save = { player -> listOf(url, player.currentPosition) },
    restore = { (savedUrl, positionMs) ->
        if (savedUrl == url) {
            inlinePlayer(context, url, playWhenReady = false, positionMs = positionMs as Long)
        } else {
            null
        }
    },
)
