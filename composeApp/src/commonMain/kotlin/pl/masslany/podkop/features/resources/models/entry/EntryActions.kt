package pl.masslany.podkop.features.resources.models.entry

interface EntryActions {
    fun onEntryVoteUpClicked(entryId: Int, voted: Boolean)
}
