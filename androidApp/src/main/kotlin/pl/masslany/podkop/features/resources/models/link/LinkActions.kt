package pl.masslany.podkop.features.resources.models.link

interface LinkActions {
    fun onLinkClicked(id: Int)
    fun onLinkUrlClicked(url: String)
    fun onLinkVoteClicked(id: Int, voted: Boolean)
    fun onLinkFavouriteClicked(linkId: Int, favourited: Boolean)
    fun onLinkReplyClicked(linkId: Int, author: String?)
    fun onLinkMoreClicked(linkId: Int, linkSlug: String)
}
