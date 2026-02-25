package pl.masslany.podkop.common.components.embed.twitter

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
import pl.masslany.podkop.common.preview.EmbedContentStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_x_logo

@Composable
fun TwitterEmbedContent(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onPreviewClick: () -> Unit,
    onFetchedContentClick: () -> Unit,
) {
    when (val twitterState = state.twitterState) {
        null,
        TwitterEmbedState.Preview,
        -> {
            EmbedThumbnailCard(
                modifier = modifier,
                thumbnailUrl = state.thumbnailUrl,
                sourceLabel = state.sourceLabel,
                onClick = onPreviewClick,
                centerOverlay = {
                    FrostedIconBadge(iconRes = Res.drawable.ic_x_logo)
                },
            )
        }

        TwitterEmbedState.Loading -> {
            EmbedThumbnailCard(
                modifier = modifier,
                thumbnailUrl = state.thumbnailUrl,
                sourceLabel = state.sourceLabel,
                onClick = {},
                enabled = false,
                centerOverlay = {
                    FrostedLoadingBadge()
                },
            )
        }

        TwitterEmbedState.Error -> {
            EmbedThumbnailCard(
                modifier = modifier,
                thumbnailUrl = state.thumbnailUrl,
                sourceLabel = state.sourceLabel,
                onClick = onPreviewClick,
                centerOverlay = {
                    FrostedIconBadge(
                        iconRes = Res.drawable.ic_x_logo,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                },
            )
        }

        is TwitterEmbedState.Loaded -> {
            TwitterTweetCard(
                modifier = modifier,
                tweet = twitterState.tweet,
                onClick = onFetchedContentClick,
            )
        }
    }
}

@Preview
@Composable
private fun TwitterEmbedContentPreview(
    @PreviewParameter(EmbedContentStateProvider::class) state: EmbedContentState,
) {
    if (state.type != EmbedContentType.Twitter) return
    PodkopPreview(darkTheme = false) {
        TwitterEmbedContent(
            modifier = Modifier.padding(16.dp),
            state = state,
            onPreviewClick = {},
            onFetchedContentClick = {},
        )
    }
}
