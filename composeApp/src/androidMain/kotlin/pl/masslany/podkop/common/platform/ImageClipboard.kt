package pl.masslany.podkop.common.platform

import android.app.Application
import androidx.compose.ui.graphics.asImageBitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ImageClipboard(private val application: Application, private val screenshotExporter: ScreenshotExporter) {
    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun copyImage(url: String): Boolean {
        val context = application
        val result = SingletonImageLoader.get(context).execute(
            ImageRequest.Builder(context)
                .data(url)
                .size(Size.ORIGINAL)
                .allowHardware(false)
                .build(),
        ) as? SuccessResult ?: return false
        val image = withContext(Dispatchers.Default) {
            result.image.toBitmap().asImageBitmap()
        }
        return screenshotExporter.copyToClipboard(image, "image_${Uuid.random()}")
    }
}
