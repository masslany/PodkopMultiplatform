package pl.masslany.podkop.common.platform

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toUri

actual class ImageDownloader(private val application: Application) {

    actual fun downloadImage(url: String): Boolean {
        val imageUri = url.toUri()
        val extensionFromUrl = imageUri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.lowercase()
            ?.takeIf { it.isNotBlank() && it.length <= 5 }

        val mimeType = application.contentResolver.getType(imageUri)
            ?: runCatching { MimeTypeMap.getFileExtensionFromUrl(url) }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }

        val extension = extensionFromUrl
            ?: mimeType
                ?.substringAfter("image/", missingDelimiterValue = "")
                ?.ifBlank { null }
            ?: "jpg"

        val baseName = imageUri.lastPathSegment
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.substringBeforeLast('.')
            ?.takeIf { it.isNotBlank() }
            ?: "image_${System.currentTimeMillis()}"
        val fileName = "$baseName.$extension"

        return runCatching {
            val request = DownloadManager.Request(imageUri)
                .setTitle(fileName)
                .setMimeType(mimeType ?: "image/*")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                )
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }.isSuccess
    }
}
