package pl.masslany.podkop.common.platform

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val PNG_COMPRESS_QUALITY = 100
private const val SCREENSHOT_FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
private const val SCREENSHOT_CACHE_DIRECTORY_NAME = "shared_screenshots"
private const val SCREENSHOT_GALLERY_DIRECTORY_NAME = "Podkop"

actual class ScreenshotExporter(private val application: Application) {

    actual suspend fun copyToClipboard(
        image: ImageBitmap,
        fileName: String,
    ): Boolean = runCatching {
        val uri = writeToCacheAndGetUri(image = image, fileName = fileName)
        val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newUri(
                application.contentResolver,
                fileName,
                uri,
            ),
        )
    }.isSuccess

    actual suspend fun shareImage(
        image: ImageBitmap,
        fileName: String,
    ): Boolean = runCatching {
        val uri = writeToCacheAndGetUri(image = image, fileName = fileName)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = SCREENSHOT_PNG_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(application.contentResolver, fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        application.startActivity(chooserIntent)
    }.isSuccess

    actual suspend fun saveToGallery(
        image: ImageBitmap,
        fileName: String,
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }

        val resolver = application.contentResolver
        val displayName = toPngScreenshotFileName(fileName = fileName)
        var uri: Uri? = null

        val isSuccess = runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, SCREENSHOT_PNG_MIME_TYPE)
                put(MediaStore.Images.Media.RELATIVE_PATH, buildGalleryRelativePath())
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val insertedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@runCatching false
            uri = insertedUri

            val isWritten = resolver.openOutputStream(insertedUri)?.use { output ->
                image.asAndroidBitmap().compress(CompressFormat.PNG, PNG_COMPRESS_QUALITY, output)
            } ?: false
            if (!isWritten) return@runCatching false

            ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }.also { pendingClearedValues ->
                resolver.update(insertedUri, pendingClearedValues, null, null)
            }
            true
        }.getOrElse { false }

        if (!isSuccess) {
            uri?.let { failedUri ->
                runCatching { resolver.delete(failedUri, null, null) }
            }
        }

        return isSuccess
    }

    private fun writeToCacheAndGetUri(
        image: ImageBitmap,
        fileName: String,
    ): Uri {
        val outputFile = writePngToCache(
            image = image,
            fileName = fileName,
        )

        return FileProvider.getUriForFile(
            application,
            "${application.packageName}$SCREENSHOT_FILE_PROVIDER_AUTHORITY_SUFFIX",
            outputFile,
        )
    }

    private fun writePngToCache(
        image: ImageBitmap,
        fileName: String,
    ): File {
        val directory = File(application.cacheDir, SCREENSHOT_CACHE_DIRECTORY_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        check(directory.exists()) { "Failed to create screenshot cache directory" }
        val outputFile = File(directory, toPngScreenshotFileName(fileName = fileName))

        FileOutputStream(outputFile).use { output ->
            check(image.asAndroidBitmap().compress(CompressFormat.PNG, PNG_COMPRESS_QUALITY, output)) {
                "Failed to encode screenshot PNG"
            }
        }
        return outputFile
    }
}

private fun buildGalleryRelativePath(): String = "${Environment.DIRECTORY_PICTURES}/$SCREENSHOT_GALLERY_DIRECTORY_NAME"
