package pl.masslany.podkop.features.linksubmission.linkdraft.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.linksubmission.models.AddLinkSuggestedImageState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_action_cancel
import podkop.composeapp.generated.resources.add_link_image_attach
import podkop.composeapp.generated.resources.add_link_image_change
import podkop.composeapp.generated.resources.add_link_image_custom_label
import podkop.composeapp.generated.resources.add_link_image_label
import podkop.composeapp.generated.resources.add_link_image_suggested_label

@Composable
internal fun LinkDraftImageSection(
    suggestedImages: ImmutableList<AddLinkSuggestedImageState>,
    selectedSuggestedImageIndex: Int?,
    photoUrl: String?,
    isMediaUploading: Boolean,
    onSuggestedImageChanged: (Int) -> Unit,
    onPhotoAttachClicked: () -> Unit,
    onPhotoRemoved: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(resource = Res.string.add_link_image_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (suggestedImages.isNotEmpty()) {
            Text(
                text = stringResource(
                    resource = Res.string.add_link_image_suggested_label,
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SuggestedImagesCarousel(
                modifier = Modifier.padding(top = 8.dp),
                images = suggestedImages,
                selectedImageIndex = selectedSuggestedImageIndex,
                onSelectedImageChanged = onSuggestedImageChanged,
            )
            Text(
                text = stringResource(resource = Res.string.add_link_image_custom_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (photoUrl.isNullOrBlank()) {
            OutlinedButton(
                onClick = onPhotoAttachClicked,
                enabled = !isMediaUploading,
            ) {
                if (isMediaUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = stringResource(resource = Res.string.add_link_image_attach))
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(onClick = onPhotoRemoved, enabled = !isMediaUploading) {
                            Text(text = stringResource(resource = Res.string.add_link_action_cancel))
                        }
                        Button(onClick = onPhotoAttachClicked, enabled = !isMediaUploading) {
                            if (isMediaUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(text = stringResource(resource = Res.string.add_link_image_change))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestedImagesCarousel(
    modifier: Modifier = Modifier,
    images: ImmutableList<AddLinkSuggestedImageState>,
    selectedImageIndex: Int?,
    onSelectedImageChanged: (Int) -> Unit,
) {
    if (images.isEmpty()) {
        return
    }

    val initialItem = selectedImageIndex?.takeIf { it in images.indices } ?: 0
    val carouselState = rememberCarouselState(
        initialItem = initialItem,
        itemCount = { images.size },
    )
    val itemShape = remember { RoundedCornerShape(16.dp) }

    LaunchedEffect(selectedImageIndex, images.size) {
        val targetItem = selectedImageIndex?.takeIf { it in images.indices } ?: 0
        if (carouselState.currentItem != targetItem) {
            carouselState.scrollToItem(targetItem)
        }
    }

    LaunchedEffect(carouselState, images.size) {
        snapshotFlow { carouselState.currentItem }
            .distinctUntilChanged()
            .collect { item ->
                if (item in images.indices) {
                    onSelectedImageChanged(item)
                }
            }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalUncontainedCarousel(
                state = carouselState,
                itemWidth = 264.dp,
                itemSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) { itemIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (itemIndex == carouselState.currentItem) {
                                Modifier.maskBorder(
                                    border = BorderStroke(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                    ),
                                    shape = itemShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .maskClip(itemShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = itemShape,
                        ),
                ) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = images[itemIndex].url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}
