package pl.masslany.podkop.common.preview

import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpTopBarActions : TopBarActions {
    override fun onTopBarBackClicked() = Unit
    override fun onTopBarProfileClicked() = Unit
    override fun onTopBarSearchClicked() = Unit
}

object NoOpPaginationActions : PaginationActions {
    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean = false
    override fun paginate() = Unit
}

object NoOpResourceItemActions : ResourceItemActions {
    override fun onEntryClicked(id: Int) = Unit
    override fun onEntryVoteUpClicked(entryId: Int, voted: Boolean) = Unit
    override fun onLinkClicked(id: Int) = Unit
    override fun onLinkUrlClicked(url: String) = Unit
    override fun onLinkVoteClicked(id: Int, voted: Boolean) = Unit
    override fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean) = Unit
    override fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean) = Unit
    override fun onProfileClicked(username: String) = Unit
    override fun onTagClicked(tag: String) = Unit
    override fun onImageClicked(url: String) = Unit
    override fun onEmbedPreviewClicked(itemId: Int, state: EmbedContentState) = Unit
}
