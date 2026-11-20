package pl.masslany.podkop.features.resourceactions

import androidx.compose.ui.graphics.ImageBitmap

interface ResourceScreenshotPreviewDialogActions {
    fun onCancelClicked()

    fun onShowParentChanged(showParent: Boolean)

    fun onScreenshotCaptured(
        image: ImageBitmap,
        action: ResourceScreenshotExportAction,
    )
}

enum class ResourceScreenshotExportAction {
    Copy,
    Share,
    Save,
}
