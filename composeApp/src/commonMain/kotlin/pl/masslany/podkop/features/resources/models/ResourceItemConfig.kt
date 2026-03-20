package pl.masslany.podkop.features.resources.models

data class ResourceItemConfig(
    val renderEntryAsCard: Boolean = true,
    val renderCommentAsCard: Boolean = false,
    val showEntryInlineActions: Boolean = true,
    val showReplyAction: Boolean = false,
    val showLinkCommentReplyAction: Boolean = false,
)
