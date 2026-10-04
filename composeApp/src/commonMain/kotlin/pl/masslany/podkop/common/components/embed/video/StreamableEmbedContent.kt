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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.components.embed.MediumMinWidth
import pl.masslany.podkop.common.components.embed.twitter.FrostedIconBadge
import pl.masslany.podkop.common.components.embed.twitter.FrostedLoadingBadge
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.StreamableEmbedState
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.embed_video_error
import podkop.composeapp.generated.resources.embed_video_open_source
import podkop.composeapp.generated.resources.embed_video_play
import podkop.composeapp.generated.resources.ic_open_in_new
import podkop.composeapp.generated.resources.ic_play_arrow

/**
 * Streamable embed rendered from [EmbedContentState.streamableState]. [onPlayClick] asks the screen
 * to play (or retry with a freshly signed url); whether that plays inline or opens the page is the
 * screen's decision. Once playback was requested, [onOpenSource] stays available as the escape hatch.
 */
@Composable
fun StreamableEmbedContent(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit,
    onOpenSource: () -> Unit,
) {
    val streamableState = state.streamableState ?: StreamableEmbedState.Preview
    // The platform player's own failure (e.g. a stalled stream); a new url from a retry clears it.
    var playerFailed by remember((streamableState as? StreamableEmbedState.Playing)?.mp4Url) {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (streamableState is StreamableEmbedState.Playing && !playerFailed) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val widthFraction = if (maxWidth >= MediumMinWidth) 0.5f else 1f
                InlineVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .aspectRatio(streamableState.aspectRatio.clampedVideoAspectRatio())
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    url = streamableState.mp4Url,
                    onError = { playerFailed = true },
                )
            }
        } else {
            StreamableThumbnail(
                state = state,
                isLoading = streamableState == StreamableEmbedState.Loading,
                isError = streamableState == StreamableEmbedState.Error || playerFailed,
                onClick = onPlayClick,
            )
        }

        if (streamableState != StreamableEmbedState.Preview) {
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
}

@Composable
private fun StreamableThumbnail(
    state: EmbedContentState,
    isLoading: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
) {
    val playLabel = stringResource(resource = Res.string.embed_video_play)
    EmbedThumbnailCard(
        modifier = Modifier.semantics {
            contentDescription = playLabel
            role = Role.Button
        },
        thumbnailUrl = state.thumbnailUrl,
        sourceLabel = state.sourceLabel,
        enabled = !isLoading,
        onClick = onClick,
        centerOverlay = {
            when {
                isLoading -> FrostedLoadingBadge()

                isError -> Column(
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

@Preview
@Composable
private fun StreamableEmbedContentErrorPreview() {
    PodkopPreview(darkTheme = false) {
        StreamableEmbedContent(
            modifier = Modifier.padding(16.dp),
            state = EmbedContentState(
                key = "streamable",
                type = EmbedContentType.Streamable,
                url = "https://streamable.com/moo",
                thumbnailUrl = "https://picsum.photos/seed/streamable/640/360",
                streamableState = StreamableEmbedState.Error,
            ),
            onPlayClick = {},
            onOpenSource = {},
        )
    }
}
