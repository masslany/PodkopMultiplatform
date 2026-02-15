package pl.masslany.podkop.common.models

data class EmbedImageState(val url: String, val source: String, val isAdult: Boolean, val isGif: Boolean)

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
