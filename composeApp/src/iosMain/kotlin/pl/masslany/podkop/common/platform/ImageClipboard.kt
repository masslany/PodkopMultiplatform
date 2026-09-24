package pl.masslany.podkop.common.platform

import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.toBitmap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ImageClipboard(private val screenshotExporter: ScreenshotExporter) {
    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun copyImage(url: String): Boolean {
        val context = PlatformContext.INSTANCE
        val result = SingletonImageLoader.get(context).execute(
            ImageRequest.Builder(context)
                .data(url)
                .size(Size.ORIGINAL)
                .build(),
        ) as? SuccessResult ?: return false
        val image = withContext(Dispatchers.Default) {
            result.image.toBitmap().asComposeImageBitmap()
        }
        return screenshotExporter.copyToClipboard(image, "image_${Uuid.random()}")
    }
}
