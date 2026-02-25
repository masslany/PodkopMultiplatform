package pl.masslany.podkop.common.components.embed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun EmbedThumbnailCard(
    thumbnailUrl: String,
    sourceLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    centerOverlay: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val imageWidthFraction = if (maxWidth >= MediumMinWidth) 0.5f else 1f
        Box(
            modifier = Modifier
                .fillMaxWidth(imageWidthFraction)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .then(
                    if (enabled) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    },
                ),
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .heightIn(min = 120.dp),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f)),
            )

            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center,
            ) {
                centerOverlay()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp),
                    ),
            ) {
                Text(
                    text = sourceLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun EmbedThumbnailCardPreview() {
    PodkopPreview(darkTheme = false) {
        EmbedThumbnailCard(
            modifier = Modifier.padding(16.dp),
            thumbnailUrl = "https://picsum.photos/seed/thumb/640/360",
            sourceLabel = "podkop.app",
            onClick = {},
            centerOverlay = {},
        )
    }
}
