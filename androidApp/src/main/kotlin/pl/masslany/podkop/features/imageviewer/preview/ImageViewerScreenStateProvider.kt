package pl.masslany.podkop.features.imageviewer.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.features.imageviewer.ImageViewerScreenState

class ImageViewerScreenStateProvider : PreviewParameterProvider<ImageViewerScreenState> {
    override val values: Sequence<ImageViewerScreenState> = sequenceOf(
        ImageViewerScreenState(imageUrl = "https://picsum.photos/seed/imageviewer/1200/800"),
        ImageViewerScreenState(imageUrl = "https://picsum.photos/seed/imageviewer2/800/1200"),
    )
}
