package pl.masslany.podkop.features.links.hits

interface HitsActions {
    fun onHitClicked(id: String)
    fun onHitVoteClicked(id: String)
}