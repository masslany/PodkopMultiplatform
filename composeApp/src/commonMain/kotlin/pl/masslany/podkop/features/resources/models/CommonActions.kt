package pl.masslany.podkop.features.resources.models

import pl.masslany.podkop.common.models.embed.EmbedContentState

interface CommonActions {
    fun onProfileClicked(username: String)
    fun onTagClicked(tag: String)
    fun onImageClicked(url: String)
    fun onEmbedPreviewClicked(itemId: Int, state: EmbedContentState)
}
