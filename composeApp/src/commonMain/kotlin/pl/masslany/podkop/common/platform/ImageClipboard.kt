package pl.masslany.podkop.common.platform

expect class ImageClipboard {
    suspend fun copyImage(url: String): Boolean
}
