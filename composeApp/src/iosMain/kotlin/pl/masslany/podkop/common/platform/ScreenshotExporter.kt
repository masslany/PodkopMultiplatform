@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class,
)

package pl.masslany.podkop.common.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

actual class ScreenshotExporter(private val viewControllerProvider: () -> UIViewController?) {

    actual suspend fun copyToClipboard(
        image: ImageBitmap,
        fileName: String,
    ): Boolean {
        val uiImage = image.toUiImage() ?: return false

        return runCatching {
            UIPasteboard.generalPasteboard.image = uiImage
        }.isSuccess
    }

    actual suspend fun shareImage(
        image: ImageBitmap,
        fileName: String,
    ): Boolean {
        val uiImage = image.toUiImage() ?: return false
        val viewController = viewControllerProvider()?.topMostPresentedViewController() ?: return false

        return withContext(Dispatchers.Main) {
            runCatching {
                val controller = UIActivityViewController(
                    activityItems = listOf(uiImage),
                    applicationActivities = null,
                )
                viewController.presentViewController(
                    viewControllerToPresent = controller,
                    animated = true,
                    completion = null,
                )
            }.isSuccess
        }
    }

    actual suspend fun saveToGallery(
        image: ImageBitmap,
        fileName: String,
    ): Boolean {
        val uiImage = image.toUiImage() ?: return false
        return withContext(Dispatchers.Main) {
            runCatching {
                UIImageWriteToSavedPhotosAlbum(
                    image = uiImage,
                    completionTarget = null,
                    completionSelector = null,
                    contextInfo = null,
                )
            }.isSuccess
        }
    }
}

private fun ImageBitmap.toUiImage(): UIImage? {
    val pngBytes = runCatching {
        Image.makeFromBitmap(asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)
            ?.bytes
    }.getOrNull() ?: return null

    val data = pngBytes.toNSData()
    return UIImage.imageWithData(data)
}

private fun UIViewController.topMostPresentedViewController(): UIViewController {
    var current = this
    while (true) {
        val next = current.presentedViewController ?: return current
        current = next
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(
        bytes = pinned.addressOf(0),
        length = size.toULong(),
    )
}
