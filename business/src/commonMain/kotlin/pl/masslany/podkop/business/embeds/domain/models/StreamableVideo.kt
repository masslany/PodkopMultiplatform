package pl.masslany.podkop.business.embeds.domain.models

data class StreamableVideo(
    val shortcode: String,
    val mp4Url: String,
    val width: Int?,
    val height: Int?,
    val thumbnailUrl: String?,
) {
    val aspectRatio: Float?
        get() = if (width != null && height != null && width > 0 && height > 0) {
            width.toFloat() / height.toFloat()
        } else {
            null
        }
}
