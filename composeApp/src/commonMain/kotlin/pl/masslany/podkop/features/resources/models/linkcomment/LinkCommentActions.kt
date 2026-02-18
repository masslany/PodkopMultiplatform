package pl.masslany.podkop.features.resources.models.linkcomment

interface LinkCommentActions {
    fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean)
}
