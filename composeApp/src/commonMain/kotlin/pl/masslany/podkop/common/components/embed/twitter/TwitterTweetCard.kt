package pl.masslany.podkop.common.components.embed.twitter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.components.embed.MediumMinWidth
import pl.masslany.podkop.common.models.embed.TwitterEmbedPreviewState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_comment
import podkop.composeapp.generated.resources.ic_favorite
import podkop.composeapp.generated.resources.ic_repeat
import podkop.composeapp.generated.resources.ic_x_logo

@Composable
fun TwitterTweetCard(
    tweet: TwitterEmbedPreviewState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val widthFraction = if (maxWidth >= MediumMinWidth) 0.5f else 1f
        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .clickable { onClick() }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tweet.avatarUrl?.takeIf { it.isNotBlank() }?.let { avatarUrl ->
                    AsyncImage(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tweet.authorName.ifBlank { "@${tweet.authorHandle}" },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "@${tweet.authorHandle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_x_logo),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = tweet.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            tweet.mediaThumbnailUrl?.takeIf { it.isNotBlank() }?.let { mediaThumbnailUrl ->
                val mediaAspectRatio = tweet.mediaAspectRatio
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (mediaAspectRatio != null) {
                                Modifier.aspectRatio(mediaAspectRatio)
                            } else {
                                Modifier.heightIn(min = 120.dp)
                            },
                        )
                        .clip(RoundedCornerShape(8.dp)),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(mediaThumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }

            if (tweet.replyCount > 0 || tweet.retweetCount > 0 || tweet.likeCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TwitterStat(
                        iconRes = Res.drawable.ic_comment,
                        count = tweet.replyCount,
                    )
                    TwitterStat(
                        iconRes = Res.drawable.ic_repeat,
                        count = tweet.retweetCount,
                    )
                    TwitterStat(
                        iconRes = Res.drawable.ic_favorite,
                        count = tweet.likeCount,
                    )
                }
            }
        }
    }
}

@Composable
private fun TwitterStat(
    iconRes: DrawableResource,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(resource = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
