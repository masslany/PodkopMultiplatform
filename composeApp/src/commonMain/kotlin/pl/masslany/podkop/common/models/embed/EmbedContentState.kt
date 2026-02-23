package pl.masslany.podkop.common.models.embed

import io.ktor.http.Url

data class EmbedContentState(
    val key: String,
    val type: EmbedContentType,
    val url: String,
    val thumbnailUrl: String,
    val twitterState: TwitterEmbedState? = null,
) {
    val sourceLabel: String
        get() = runCatching { Url(url).host.removePrefix("www.") }
            .getOrElse { type.name.lowercase() }
}

enum class EmbedContentType {
    Twitter,
    Youtube,
    Streamable,
    Other,
}

sealed interface TwitterEmbedState {
    data object Preview : TwitterEmbedState
    data object Loading : TwitterEmbedState
    data object Error : TwitterEmbedState
    data class Loaded(val tweet: TwitterEmbedPreviewState) : TwitterEmbedState
}

internal fun String.toEmbedContentType(): EmbedContentType = when (this.lowercase()) {
    "twitter" -> EmbedContentType.Twitter
    "youtube" -> EmbedContentType.Youtube
    "streamable" -> EmbedContentType.Streamable
    else -> EmbedContentType.Other
}

data class TwitterEmbedPreviewState(
    val authorName: String,
    val authorHandle: String,
    val avatarUrl: String?,
    val text: String,
    val replyCount: Int,
    val retweetCount: Int,
    val likeCount: Int,
    val mediaThumbnailUrl: String?,
    val mediaAspectRatio: Float?,
)
