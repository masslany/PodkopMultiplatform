package pl.masslany.podkop.features.resources.models.entrycomment

interface EntryCommentActions {
    fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean)
    fun onEntryCommentMoreClicked(entryId: Int, entryCommentId: Int)
}
