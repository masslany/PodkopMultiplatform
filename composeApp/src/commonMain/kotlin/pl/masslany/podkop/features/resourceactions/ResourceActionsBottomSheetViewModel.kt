package pl.masslany.podkop.features.resourceactions

import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_add
import podkop.composeapp.generated.resources.ic_copy
import podkop.composeapp.generated.resources.resource_actions_copy_as_link
import podkop.composeapp.generated.resources.resource_actions_show_voters
import podkop.composeapp.generated.resources.snackbar_link_copied

class ResourceActionsBottomSheetViewModel(
    private val params: ResourceActionsParams,
    private val appNavigator: AppNavigator,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ResourceActionsBottomSheetActions {

    private val _state = MutableStateFlow(
        buildState(this.params),
    )
    val state = _state.asStateFlow()

    override fun onActionClicked(actionId: ResourceActionId) {
        when (actionId) {
            ResourceActionId.CopyAsLink -> {
                snackbarManager.tryEmit(
                    SnackbarEvent(
                        message = SnackbarMessage.Resource(Res.string.snackbar_link_copied),
                    ),
                )
                appNavigator.back()
            }

            ResourceActionId.ShowVoters -> {
                val target = when (params.resourceType) {
                    ResourceActionsType.Entry -> ResourceVotesBottomSheetScreen.forEntry(
                        entryId = params.rootId,
                    )

                    ResourceActionsType.EntryComment -> ResourceVotesBottomSheetScreen.forEntryComment(
                        entryId = params.rootId,
                        entryCommentId = requireNotNull(params.childId) { "Entry comment actions require childId" },
                    )

                    ResourceActionsType.LinkComment -> return
                }

                appNavigator.back()
                appNavigator.navigateTo(target)
            }
        }
    }
}

private fun buildState(params: ResourceActionsParams): ResourceActionsBottomSheetState {
    val showVotersAction = ResourceActionItemState(
        id = ResourceActionId.ShowVoters,
        title = Res.string.resource_actions_show_voters,
        icon = Res.drawable.ic_add,
    )
    val copyLinkAction = ResourceActionItemState(
        id = ResourceActionId.CopyAsLink,
        title = Res.string.resource_actions_copy_as_link,
        icon = Res.drawable.ic_copy,
        localAction = ResourceActionLocalAction.CopyToClipboard(
            value = buildResourceLink(
                params = params,
            ),
        ),
    )

    return ResourceActionsBottomSheetState(
        actions = when (params.resourceType) {
            ResourceActionsType.Entry,
            ResourceActionsType.EntryComment,
            -> persistentListOf(
                showVotersAction,
                copyLinkAction,
            )

            ResourceActionsType.LinkComment,
            -> persistentListOf(copyLinkAction)
        },
    )
}

private fun buildResourceLink(params: ResourceActionsParams): String = when (params.resourceType) {
    ResourceActionsType.Entry -> "https://wykop.pl/wpis/${params.rootId}"

    ResourceActionsType.EntryComment -> {
        val entryCommentId = requireNotNull(params.childId) { "Entry comment actions require childId" }
        "https://wykop.pl/wpis/${params.rootId}/#$entryCommentId"
    }

    ResourceActionsType.LinkComment -> {
        val linkCommentId = requireNotNull(params.childId) { "Link comment actions require childId" }
        val slug = requireNotNull(params.rootSlug) { "Link comment actions require rootSlug" }
        val parentCommentId = params.parentId?.takeIf { it > 0 && it != linkCommentId }
        if (parentCommentId == null) {
            "https://wykop.pl/link/${params.rootId}/$slug/komentarz/$linkCommentId"
        } else {
            "https://wykop.pl/link/${params.rootId}/$slug/komentarz/$parentCommentId#$linkCommentId"
        }
    }
}
