package pl.masslany.podkop.features.resources.models.linkcomment

interface LinkCommentActions {
    fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean)
    fun onLinkCommentVoteDownClick(linkId: Int, commentId: Int, voted: Boolean)
    fun onLinkCommentFavouriteClicked(linkId: Int, commentId: Int, favourited: Boolean)
    fun onLinkCommentReplyClicked(linkId: Int, commentId: Int, author: String?)
    fun onLinkCommentMoreClicked(
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    )
}
