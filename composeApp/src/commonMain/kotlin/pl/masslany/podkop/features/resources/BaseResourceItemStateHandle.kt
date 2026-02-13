package pl.masslany.podkop.features.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entry.EntryVoteAction
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

open class BaseResourceItemStateHolder(
    private val linksRepository: LinksRepository,
    private val entriesRepository: EntriesRepository,
    private val appNavigator: AppNavigator,
) : ResourceItemStateHolder {

    private val _items = MutableStateFlow<ImmutableList<ResourceItemState>>(persistentListOf())
    override val items = _items.asStateFlow()

    protected var scope: CoroutineScope? = null

    private var isUpcoming: Boolean = false

    override fun init(
        scope: CoroutineScope,
        isUpcoming: Boolean,
    ) {
        this.scope = scope
        this.isUpcoming = isUpcoming
    }

    override fun updateData(data: List<ResourceItem>) {
        _items.value = data.map { it.toResourceItemState(isUpcoming) }.toImmutableList()
    }

    override fun appendData(data: List<ResourceItem>) {
        _items.update { list ->
            (list + data.map { it.toResourceItemState(isUpcoming) }).toImmutableList()
        }
    }

    override fun onLinkVoteClicked(id: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                linksRepository.voteOnLink(linkId = id)
            } else {
                linksRepository.removeVoteOnLink(linkId = id)
            }

            result.onSuccess {
                linksRepository.getLink(id)
                    .onSuccess {
                        println("DBG ---> link updated: $it")
                        notifyItemUpdated(it.data)
                    }
                    .onFailure {
                        println("DBG ---> link updated failed: $it")
                    }
            }
        }
    }

    fun onLinkCommentVoted(
        linkId: Int,
        commentId: Int,
        voted: Boolean,
    ) {
        val item = (_items.value.find { it.id == linkId } as? LinkItemState)
        // todo think this through
    }

    override fun onLinkClicked(id: Int) {
        println("DBG ---> link clicked: $id")
    }

    override fun onLinkUrlClicked(url: String) {
        appNavigator.openExternalLink(url)
    }

    override fun onTagClicked(tag: String) {
        println("DBG ---> tag clicked: $tag")
    }

    override fun onProfileClicked(username: String) {
        println("DBG ---> ic_profile clicked: $username")
    }

    override fun onEntryVoteUpClicked(entryId: Int, voted: Boolean) {
        scope?.launch {
            val result = if (voted) {
                entriesRepository.voteUp(entryId)
            } else {
                entriesRepository.removeVoteUp(entryId)
            }

            result.onSuccess {
                updateEntryVote(
                    entryId = entryId,
                    action = if (voted) {
                        EntryVoteAction.VoteUp
                    } else {
                        EntryVoteAction.RemoveVoteUp
                    },
                )
            }
        }
    }

    override fun onEntryClicked(id: Int) {
        appNavigator.navigateTo(EntryDetailsScreen(id))
    }

    override fun onEntryCommentVoteUpClick(entryCommentId: Int, parentEntryId: Int, voted: Boolean) {
    }

    private fun updateEntryVote(
        entryId: Int,
        action: EntryVoteAction,
    ) {
        updateItem(entryId) { item ->
            val entry = item as? EntryItemState ?: return@updateItem item

            val newVoteState = when (action) {
                EntryVoteAction.VoteUp ->
                    entry.voteState.increaseVoteUp()

                EntryVoteAction.RemoveVoteUp ->
                    entry.voteState.removeVoteUp()
            }

            entry.copy(voteState = newVoteState)
        }
    }

    protected fun updateItem(
        id: Int,
        updater: (ResourceItemState) -> ResourceItemState,
    ) {
        _items.update { list ->
            list.map { item ->
                if (item.id == id) updater(item) else item
            }.toImmutableList()
        }
    }

    // This is open so specialized handlers can update multiple lists
    open fun notifyItemUpdated(newState: ResourceItem) {
        _items.update { list ->
            list.map {
                if (it.id ==
                    newState.id
                ) {
                    newState.toResourceItemState(isUpcoming)
                } else {
                    it
                }
            }.toImmutableList()
        }
    }
}
