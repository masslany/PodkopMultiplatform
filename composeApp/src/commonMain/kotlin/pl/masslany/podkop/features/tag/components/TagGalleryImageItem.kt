package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.features.tag.TagActions
import pl.masslany.podkop.features.tag.TagGalleryItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_tag_gallery_open_entry
import podkop.composeapp.generated.resources.embed_gif_badge
import podkop.composeapp.generated.resources.ic_open_in_new
import podkop.composeapp.generated.resources.links_screen_label_adult_rating

@Composable
fun TagGalleryImageItem(
    item: TagGalleryItemState,
    actions: TagActions,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { actions.onImageClicked(item.imageUrl) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .aspectRatio(item.galleryAspectRatio),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (item.isAdult) {
                    TagGalleryBadge(label = stringResource(Res.string.links_screen_label_adult_rating))
                }
                if (item.isGif) {
                    TagGalleryBadge(label = stringResource(Res.string.embed_gif_badge))
                }
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                onClick = { actions.onEntryClicked(item.entryId) },
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = vectorResource(resource = Res.drawable.ic_open_in_new),
                    contentDescription = stringResource(
                        resource = Res.string.accessibility_tag_gallery_open_entry,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TagGalleryBadge(
    label: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
