package pl.masslany.podkop.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Scale
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.EmbedImageState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.embed_adult_image
import podkop.composeapp.generated.resources.embed_image_source

@Composable
fun EmbedImage(
    modifier: Modifier = Modifier,
    state: EmbedImageState,
    onImageClick: () -> Unit,
) {
    val context = LocalPlatformContext.current
    val sizeResolver = rememberConstraintsSizeResolver()
    var isAdultOverlayVisible by rememberSaveable { mutableStateOf(state.isAdult) }
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                modifier = Modifier
                    .then(sizeResolver)
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clickable {
                        if (isAdultOverlayVisible) {
                            isAdultOverlayVisible = false
                        } else {
                            onImageClick()
                        }
                    }
                    .then(
                        if (isAdultOverlayVisible) {
                            Modifier.hazeEffect { blurEnabled = true }
                        } else {
                            Modifier
                        },
                    ),
                model = ImageRequest.Builder(context)
                    .data(state.url)
                    .memoryCacheKey(state.url)
                    .diskCacheKey(state.url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .scale(Scale.FIT)
                    .size(sizeResolver)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
            if (isAdultOverlayVisible) {
                Text(
                    text = stringResource(resource = Res.string.embed_adult_image),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 6f,
                                join = StrokeJoin.Round,
                            ),
                        ),
                )

                Text(
                    text = stringResource(resource = Res.string.embed_adult_image),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = stringResource(
                resource = Res.string.embed_image_source,
                state.source,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
