package pl.masslany.podkop.common.models

data class EmbedImageState(
    val url: String,
    val key: String?,
    val source: String,
    val isAdult: Boolean,
    val isGif: Boolean,
    val width: Int? = null,
    val height: Int? = null,
) {
    val aspectRatio: Float?
        get() {
            val w = width ?: return null
            val h = height ?: return null
            if (w <= 0 || h <= 0) return null
            return w.toFloat() / h.toFloat()
        }
}

fun isGifImage(
    url: String,
    mimeType: String?,
): Boolean {
    if (mimeType.equals("image/gif", ignoreCase = true)) {
        return true
    }

    return url
        .substringBefore('?')
        .endsWith(".gif", ignoreCase = true)
}
