package pl.masslany.podkop.common.platform

import androidx.compose.ui.graphics.ImageBitmap

expect class ScreenshotExporter {
    suspend fun copyToClipboard(
        image: ImageBitmap,
        fileName: String,
    ): Boolean

    suspend fun shareImage(
        image: ImageBitmap,
        fileName: String,
    ): Boolean

    suspend fun saveToGallery(
        image: ImageBitmap,
        fileName: String,
    ): Boolean
}
