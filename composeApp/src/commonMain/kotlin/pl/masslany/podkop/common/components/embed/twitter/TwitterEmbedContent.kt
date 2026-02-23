package pl.masslany.podkop.common.components.embed.twitter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.TwitterEmbedState
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
