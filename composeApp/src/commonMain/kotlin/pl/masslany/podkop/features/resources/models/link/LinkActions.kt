package pl.masslany.podkop.features.resources.models.link

interface LinkActions {
    fun onLinkClicked(id: Int)
    fun onLinkVoteClicked(id: Int, voted: Boolean)
}
