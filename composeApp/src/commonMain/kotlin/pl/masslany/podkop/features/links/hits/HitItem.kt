package pl.masslany.podkop.features.links.hits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.components.AdultRating
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.links.hits.preview.HitItemStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.no_image

@Composable
fun HitItem(
    modifier: Modifier = Modifier,
    state: HitItemState,
    onItemClick: () -> Unit,
    onVoteClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(120.dp)
            .clip(CardDefaults.shape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .clickable { onItemClick() },
    ) {
        if (state.imageUrl.isEmpty()) {
            Image(
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
                    .align(Alignment.Center),
                painter = painterResource(resource = Res.drawable.no_image),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        } else {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(state.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        Count(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            state = state.countState,
            backgroundColor = MaterialTheme.colorScheme.background,
            onClick = onVoteClick,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        startY = 0f,
                        endY = 100f,
                    ),
                )
                .align(Alignment.BottomCenter),
        )
        Title(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp,
                ),
            state = state.titleState,
            textColor = Color.White,
            textStyle = MaterialTheme.typography.bodySmall,
        )
        if (state.isAdult) {
            AdultRating(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd),
            )
        }
    }
}

@Preview
@Composable
private fun HitItemPreview(
    @PreviewParameter(HitItemStateProvider::class) state: HitItemState,
) {
    PodkopPreview(darkTheme = false) {
        HitItem(
            modifier = Modifier.padding(16.dp),
            state = state,
            onItemClick = {},
            onVoteClick = {},
        )
    }
}
