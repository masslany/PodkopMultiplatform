package pl.masslany.podkop.features.imageviewer

import pl.masslany.podkop.common.platform.ImageClipboard
import pl.masslany.podkop.common.platform.ImageDownloader

interface ImageExportService {
    suspend fun copyImage(url: String): Boolean
    fun downloadImage(url: String): Boolean
}

class DefaultImageExportService(
    private val imageClipboard: ImageClipboard,
    private val imageDownloader: ImageDownloader,
) : ImageExportService {
    override suspend fun copyImage(url: String): Boolean = imageClipboard.copyImage(url)
    override fun downloadImage(url: String): Boolean = imageDownloader.downloadImage(url)
}
