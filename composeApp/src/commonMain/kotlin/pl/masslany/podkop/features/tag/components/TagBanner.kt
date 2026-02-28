package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

private val TagBannerHeight = 160.dp

@Composable
fun TagBanner(
    bannerUrl: String,
) {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(TagBannerHeight),
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(bannerUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        error = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
    )
}
