package pl.masslany.podkop.features.resources

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdate
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdatesStore
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resources.models.toDeletedByAuthor
import pl.masslany.podkop.features.tag.TagScreen

class ResourceItemStateHolderImpl(
    linksRepository: LinksRepository,
    entriesRepository: EntriesRepository,
    favouritesRepository: FavouritesRepository,
    private val appNavigator: AppNavigator,
    dispatcherProvider: DispatcherProvider,
    private val logger: AppLogger,
    twitterEmbedPreviewRepository: TwitterEmbedPreviewRepository,
    streamableEmbedPlayback: StreamableEmbedPlayback,
    screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
    private val resourceActionUpdatesStore: ResourceActionUpdatesStore,
    isUpcoming: Boolean = false,
    // Lets an owner mirror refreshed items into lists of its own, such as the links feed's hits.
    private val onItemRefreshed: (ResourceItem) -> Unit = {},
) : ResourceItemStateHolder {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.mainImmediate)

    private val store = ResourceItemsStore(dispatcherProvider, isUpcoming)
    private val voting = ResourceVoting(linksRepository, entriesRepository, store, logger)
    private val favourites = ResourceFavourites(favouritesRepository, store, logger)
    private val menus = ResourceItemMenus(store, appNavigator, screenshotShareDraftStore)
    private val embedPreviews = ResourceEmbedPreviews(
        appNavigator = appNavigator,
        twitterEmbedPreviewRepository = twitterEmbedPreviewRepository,
        streamableEmbedPlayback = streamableEmbedPlayback,
        logger = logger,
        updateEmbedState = store::updateEmbed,
    )

    override val items = store.items

    init {
        observeResourceActionUpdates()
    }

    private fun observeResourceActionUpdates() {
        scope.launch {
            resourceActionUpdatesStore.updates.collect { update ->
                when (update) {
                    is ResourceActionUpdate.EntryCommentDeleted -> {
                        store.update(update.commentId) { it.toDeletedByAuthor() }
                    }

                    is ResourceActionUpdate.ResourceEdited -> {
                        store.applyEdited(update.resource)
                    }
                }
            }
        }
    }

    override suspend fun updateData(data: List<ResourceItem>) = store.replaceAll(data)

    override suspend fun appendData(data: List<ResourceItem>) = store.append(data)

    override suspend fun notifyItemUpdated(newState: ResourceItem) {
        store.replace(newState)
        onItemRefreshed(newState)
    }

    override fun onLinkClicked(id: Int) {
        appNavigator.navigateTo(LinkDetailsScreen(id))
    }

    override fun onLinkUrlClicked(url: String) {
        appNavigator.openLink(url)
    }

    override fun onLinkVoteClicked(id: Int, voted: Boolean) = launch {
        voting.voteLink(id, voted)?.let { notifyItemUpdated(it) }
    }

    override fun onLinkFavouriteClicked(linkId: Int, favourited: Boolean) = launch {
        favourites.toggle(linkId, FavouriteType.Link, favourited)
    }

    override fun onLinkReplyClicked(linkId: Int, author: String?) = Unit

    override fun onLinkMoreClicked(linkId: Int, linkSlug: String) = Unit

    override fun onLinkCommentVoteUpClick(linkId: Int, commentId: Int, voted: Boolean) = launch {
        voting.voteLinkCommentUp(linkId, commentId, voted)
    }

    override fun onLinkCommentVoteDownClick(linkId: Int, commentId: Int, voted: Boolean) = launch {
        voting.voteLinkCommentDown(linkId, commentId, voted)
    }

    override fun onLinkCommentFavouriteClicked(linkId: Int, commentId: Int, favourited: Boolean) = launch {
        favourites.toggle(commentId, FavouriteType.LinkComment, favourited)
    }

    override fun onLinkCommentReplyClicked(linkId: Int, commentId: Int, author: String?) = Unit

    override fun onLinkCommentLongClicked(linkId: Int, commentId: Int) {
        menus.openLinkCommentTextSelection(commentId)
    }

    override fun onLinkCommentMoreClicked(
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    ) {
        menus.openLinkCommentActions(linkId, commentId, linkSlug, parentCommentId)
    }

    override fun onEntryClicked(id: Int) {
        appNavigator.navigateTo(EntryDetailsScreen.forEntry(id))
    }

    override fun onEntryLongClicked(id: Int) {
        menus.openEntryTextSelection(id)
    }

    override fun onEntryVoteUpClicked(entryId: Int, voted: Boolean) = launch {
        voting.voteEntry(entryId, voted)
    }

    override fun onEntrySurveyVoteClicked(entryId: Int, optionNumber: Int) = launch {
        voting.voteEntrySurvey(entryId, optionNumber)
    }

    override fun onEntryFavouriteClicked(entryId: Int, favourited: Boolean) = launch {
        favourites.toggle(entryId, FavouriteType.Entry, favourited)
    }

    override fun onEntryReplyClicked(entryId: Int, author: String?) {
        appNavigator.navigateTo(EntryDetailsScreen.forEntryReply(entryId = entryId, author = author))
    }

    override fun onEntryMoreClicked(entryId: Int) {
        menus.openEntryActions(entryId)
    }

    override fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean) = launch {
        voting.voteEntryComment(entryId = parentEntryId, commentId = entryCommentId, voted = voted)
    }

    override fun onEntryCommentFavouriteClicked(entryCommentId: Int, favourited: Boolean) = launch {
        favourites.toggle(entryCommentId, FavouriteType.EntryComment, favourited)
    }

    override fun onEntryCommentReplyClicked(entryId: Int, entryCommentId: Int, author: String?) {
        appNavigator.navigateTo(
            EntryDetailsScreen.forEntryCommentReply(
                entryId = entryId,
                entryCommentId = entryCommentId,
                author = author,
            ),
        )
    }

    override fun onEntryCommentLongClicked(entryId: Int, entryCommentId: Int) {
        menus.openEntryCommentTextSelection(entryId, entryCommentId)
    }

    override fun onEntryCommentMoreClicked(entryId: Int, entryCommentId: Int) {
        menus.openEntryCommentActions(entryId, entryCommentId)
    }

    override fun onTagClicked(tag: String) {
        appNavigator.navigateTo(TagScreen(tag = tag))
    }

    override fun onProfileClicked(username: String) {
        appNavigator.navigateTo(ProfileScreen(username = username))
    }

    override fun onImageClicked(url: String) {
        appNavigator.navigateTo(ImageViewerScreen(imageUrl = url))
    }

    override fun onEmbedPreviewClicked(itemId: Int, state: EmbedContentState) {
        embedPreviews.onPreviewClicked(scope, itemId, state)
    }

    override fun close() {
        scope.cancel()
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(block = block)
    }
}
