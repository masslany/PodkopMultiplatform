package pl.masslany.podkop.features.resources.models.entrycomment

interface EntryCommentActions {
    fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean)
    fun onEntryCommentFavouriteClicked(entryCommentId: Int, favourited: Boolean)
    fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?)
    fun onEntryCommentLongClicked(entryId: Int, entryCommentId: Int)
    fun onEntryCommentMoreClicked(entryId: Int, entryCommentId: Int)
}
