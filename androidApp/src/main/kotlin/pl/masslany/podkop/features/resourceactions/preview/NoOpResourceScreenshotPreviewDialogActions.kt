package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.graphics.ImageBitmap
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotExportAction
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogActions

object NoOpResourceScreenshotPreviewDialogActions : ResourceScreenshotPreviewDialogActions {
    override fun onCancelClicked() = Unit

    override fun onShowParentChanged(showParent: Boolean) = Unit

    override fun onScreenshotCaptured(
        image: ImageBitmap,
        action: ResourceScreenshotExportAction,
    ) = Unit
}
