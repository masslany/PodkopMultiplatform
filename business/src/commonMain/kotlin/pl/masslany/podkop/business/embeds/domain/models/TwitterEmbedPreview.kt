package pl.masslany.podkop.business.embeds.domain.models

data class TwitterEmbedPreview(
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
