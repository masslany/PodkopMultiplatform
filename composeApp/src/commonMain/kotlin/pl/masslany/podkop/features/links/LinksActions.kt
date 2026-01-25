package pl.masslany.podkop.features.links

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.links.hits.HitsActions
import pl.masslany.podkop.features.resources.models.ResourceItemActions

@Stable
interface LinksActions : HitsActions, ResourceItemActions {
    fun onLinkClicked(id: String)
    fun onLinkVoteClicked(id: String)
}