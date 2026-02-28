package pl.masslany.podkop.features.resources.models.entry

interface EntryActions {
    fun onEntryClicked(id: Int)
    fun onEntryVoteUpClicked(entryId: Int, voted: Boolean)
    fun onEntryReplyClicked(entryId: Int, author: String?)
    fun onEntryMoreClicked(entryId: Int)
}
