package pl.masslany.podkop.common.components.embed.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository
import pl.masslany.podkop.business.embeds.domain.models.StreamableVideo
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.components.embed.MediumMinWidth
import pl.masslany.podkop.common.components.embed.twitter.FrostedIconBadge
import pl.masslany.podkop.common.components.embed.twitter.FrostedLoadingBadge
import pl.masslany.podkop.common.models.embed.EmbedContentState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.embed_video_error
import podkop.composeapp.generated.resources.embed_video_open_source
import podkop.composeapp.generated.resources.embed_video_play
import podkop.composeapp.generated.resources.ic_open_in_new
import podkop.composeapp.generated.resources.ic_play_arrow

private sealed interface StreamablePlaybackState {
    data object Idle : StreamablePlaybackState
    data object Loading : StreamablePlaybackState
    data object Error : StreamablePlaybackState
    data class Ready(val video: StreamableVideo) : StreamablePlaybackState
}

/**
 * Streamable embed played inside the post. The MP4 is resolved on tap because Streamable signs it
 * with a short expiry. [onOpenSource] stays available in every state as the escape hatch.
 */
@Composable
fun StreamableEmbedContent(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onOpenSource: () -> Unit,
) {
    val repository = koinInject<StreamableVideoRepository>()
    val scope = rememberCoroutineScope()
    var playback by remember(state.url) { mutableStateOf<StreamablePlaybackState>(StreamablePlaybackState.Idle) }
    val playLabel = stringResource(resource = Res.string.embed_video_play)

    fun load() {
        playback = StreamablePlaybackState.Loading
        scope.launch {
            playback = repository.getVideo(state.url).fold(
                onSuccess = { StreamablePlaybackState.Ready(it) },
                onFailure = { StreamablePlaybackState.Error },
            )
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (val current = playback) {
            is StreamablePlaybackState.Ready -> BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val widthFraction = if (maxWidth >= MediumMinWidth) 0.5f else 1f
                val aspectRatio = (current.video.aspectRatio ?: DefaultVideoAspectRatio).clampedVideoAspectRatio()
                InlineVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    url = current.video.mp4Url,
                    onError = { playback = StreamablePlaybackState.Error },
                )
            }

            else -> EmbedThumbnailCard(
                modifier = Modifier.semantics {
                    contentDescription = playLabel
                    role = Role.Button
                },
                thumbnailUrl = state.thumbnailUrl,
                sourceLabel = state.sourceLabel,
                enabled = current != StreamablePlaybackState.Loading,
                onClick = ::load,
                centerOverlay = {
                    when (current) {
                        StreamablePlaybackState.Loading -> FrostedLoadingBadge()
                        StreamablePlaybackState.Error -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FrostedIconBadge(iconRes = Res.drawable.ic_play_arrow)
                            Text(
                                text = stringResource(resource = Res.string.embed_video_error),
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                            )
                        }

                        else -> FrostedIconBadge(iconRes = Res.drawable.ic_play_arrow)
                    }
                },
            )
        }

        TextButton(onClick = onOpenSource) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_open_in_new),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(resource = Res.string.embed_video_open_source, state.sourceLabel),
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
