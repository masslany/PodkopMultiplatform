package pl.masslany.podkop.common.models.embed

import pl.masslany.podkop.business.common.domain.models.common.Embed
import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview

internal fun Embed?.toEmbedContentState(): EmbedContentState? {
    val embed = this ?: return null
    if (embed.url.isBlank()) return null
    if (embed.thumbnail.isBlank()) return null

    val type = embed.type.toEmbedContentType()

    return EmbedContentState(
        key = embed.key,
        type = type,
        url = embed.url,
        thumbnailUrl = embed.thumbnail,
        twitterState = if (type == EmbedContentType.Twitter) {
            TwitterEmbedState.Preview
        } else {
            null
        },
    )
}

internal fun TwitterEmbedPreview.toTwitterEmbedPreviewState() = TwitterEmbedPreviewState(
    authorName = authorName,
    authorHandle = authorHandle,
    avatarUrl = avatarUrl,
    text = text,
    replyCount = replyCount,
    retweetCount = retweetCount,
    likeCount = likeCount,
    mediaThumbnailUrl = mediaThumbnailUrl,
    mediaAspectRatio = mediaAspectRatio,
)
