package pl.masslany.podkop.common.platform

expect class ImageDownloader {
    fun downloadImage(url: String): Boolean
}
