package pl.masslany.podkop.features.links

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.links.hits.HitsActions

@Stable
interface LinksActions : HitsActions {
    fun onLinkClicked(id: String)
    fun onLinkVoteClicked(id: String)
}