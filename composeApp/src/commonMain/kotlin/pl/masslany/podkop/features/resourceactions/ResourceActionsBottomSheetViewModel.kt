package pl.masslany.podkop.features.resourceactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerPrefill
import pl.masslany.podkop.features.composer.ComposerRequest
import pl.masslany.podkop.features.composer.ComposerResult
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_delete
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.dialog_title_delete_entry
import podkop.composeapp.generated.resources.dialog_title_delete_entry_comment
import podkop.composeapp.generated.resources.ic_add
import podkop.composeapp.generated.resources.ic_arrow_down
import podkop.composeapp.generated.resources.ic_arrow_up
import podkop.composeapp.generated.resources.ic_copy
import podkop.composeapp.generated.resources.ic_delete
import podkop.composeapp.generated.resources.ic_edit
import podkop.composeapp.generated.resources.ic_share
import podkop.composeapp.generated.resources.resource_actions_copy_as_link
import podkop.composeapp.generated.resources.resource_actions_delete_entry
import podkop.composeapp.generated.resources.resource_actions_delete_entry_comment
import podkop.composeapp.generated.resources.resource_actions_edit_comment
import podkop.composeapp.generated.resources.resource_actions_edit_entry
import podkop.composeapp.generated.resources.resource_actions_share_as_screenshot
import podkop.composeapp.generated.resources.resource_actions_show_link_downvoters
import podkop.composeapp.generated.resources.resource_actions_show_link_upvoters
import podkop.composeapp.generated.resources.resource_actions_show_voters
import podkop.composeapp.generated.resources.snackbar_entry_comment_deleted
import podkop.composeapp.generated.resources.snackbar_entry_deleted
import podkop.composeapp.generated.resources.snackbar_link_copied

class ResourceActionsBottomSheetViewModel(
    private val params: ResourceActionsParams,
    private val authRepository: AuthRepository,
    private val entriesRepository: EntriesRepository,
    private val appNavigator: AppNavigator,
    private val snackbarManager: SnackbarManager,
    private val screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
    private val resourceActionUpdatesStore: ResourceActionUpdatesStore,
) : ViewModel(),
    ResourceActionsBottomSheetActions {

    private val _state = MutableStateFlow(
        buildState(params = this.params),
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

            ResourceActionId.ShareAsScreenshot -> {
                val draftId = params.screenshotDraftId ?: return
                appNavigator.navigateTo(
                    ResourceScreenshotPreviewDialogScreen(
                        draftId = draftId,
                    ),
                )
            }

            ResourceActionId.ShowVoters -> {
                val target = when (params.resourceType) {
                    ResourceActionsType.Link -> return

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

            ResourceActionId.ShowLinkUpvoters -> {
                appNavigator.back()
                appNavigator.navigateTo(ResourceVotesBottomSheetScreen.forLinkUpvotes(linkId = params.rootId))
            }

            ResourceActionId.ShowLinkDownvoters -> {
                appNavigator.back()
                appNavigator.navigateTo(ResourceVotesBottomSheetScreen.forLinkDownvotes(linkId = params.rootId))
            }

            ResourceActionId.DeleteEntry -> onDeleteEntryClicked()

            ResourceActionId.DeleteEntryComment -> onDeleteEntryCommentClicked()

            ResourceActionId.EditEntry -> onEditEntryClicked()

            ResourceActionId.EditEntryComment -> onEditEntryCommentClicked()

            ResourceActionId.EditLinkComment -> onEditLinkCommentClicked()
        }
    }

    override fun onCleared() {
        params.screenshotDraftId?.let(screenshotShareDraftStore::remove)
        super.onCleared()
    }

    private fun onDeleteEntryClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) return@launch

            val dialog = GenericDialog.fromResources(
                title = Res.string.dialog_title_delete_entry,
                positiveText = Res.string.dialog_button_delete,
                negativeText = Res.string.dialog_button_dismiss,
            )
            val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
            if (!confirmed) return@launch

            entriesRepository.deleteEntry(entryId = params.rootId)
                .onSuccess {
                    val openedFromEntryDetails = isOpenedFromEntryDetails()
                    appNavigator.back()
                    if (openedFromEntryDetails) {
                        appNavigator.back()
                    }
                    snackbarManager.tryEmit(
                        SnackbarEvent(
                            message = SnackbarMessage.Resource(Res.string.snackbar_entry_deleted),
                        ),
                    )
                }
                .onFailure {
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun onDeleteEntryCommentClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) return@launch
            val commentId = params.childId ?: return@launch

            val dialog = GenericDialog.fromResources(
                title = Res.string.dialog_title_delete_entry_comment,
                positiveText = Res.string.dialog_button_delete,
                negativeText = Res.string.dialog_button_dismiss,
            )
            val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
            if (!confirmed) return@launch

            entriesRepository.deleteEntryComment(
                entryId = params.rootId,
                commentId = commentId,
            ).onSuccess {
                resourceActionUpdatesStore.tryEmit(
                    ResourceActionUpdate.EntryCommentDeleted(commentId),
                )
                appNavigator.back()
                snackbarManager.tryEmit(
                    SnackbarEvent(
                        message = SnackbarMessage.Resource(Res.string.snackbar_entry_comment_deleted),
                    ),
                )
            }.onFailure {
                snackbarManager.tryEmitGenericError()
            }
        }
    }

    private fun onEditEntryClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn() || !params.canEdit) return@launch
            val request = buildEditComposerRequest(params) as? ComposerRequest.EditEntry ?: return@launch

            val resultKey = "resource-actions-edit-entry-${params.rootId}-${kotlin.random.Random.nextInt()}"
            val result = appNavigator.awaitResult<ComposerResult>(
                target = ComposerBottomSheetScreen(
                    resultKey = resultKey,
                    request = request,
                ),
                key = resultKey,
            )

            if (result is ComposerResult.Submitted) {
                resourceActionUpdatesStore.tryEmit(ResourceActionUpdate.ResourceEdited(result.resource))
            }
            appNavigator.back()
        }
    }

    private fun onEditEntryCommentClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn() || !params.canEdit) return@launch
            val request = buildEditComposerRequest(params) as? ComposerRequest.EditEntryComment ?: return@launch
            val commentId = params.childId ?: return@launch

            val resultKey = buildString {
                append("resource-actions-edit-entry-comment-")
                append(params.rootId)
                append('-')
                append(commentId)
                append('-')
                append(kotlin.random.Random.nextInt())
            }
            val result = appNavigator.awaitResult<ComposerResult>(
                target = ComposerBottomSheetScreen(
                    resultKey = resultKey,
                    request = request,
                ),
                key = resultKey,
            )

            if (result is ComposerResult.Submitted) {
                resourceActionUpdatesStore.tryEmit(ResourceActionUpdate.ResourceEdited(result.resource))
            }
            appNavigator.back()
        }
    }

    private fun onEditLinkCommentClicked() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn() || !params.canEdit) return@launch
            val request = buildEditComposerRequest(params) as? ComposerRequest.EditLinkComment ?: return@launch
            val commentId = params.childId ?: return@launch

            val resultKey = buildString {
                append("resource-actions-edit-link-comment-")
                append(params.rootId)
                append('-')
                append(commentId)
                append('-')
                append(kotlin.random.Random.nextInt())
            }
            val result = appNavigator.awaitResult<ComposerResult>(
                target = ComposerBottomSheetScreen(
                    resultKey = resultKey,
                    request = request,
                ),
                key = resultKey,
            )

            if (result is ComposerResult.Submitted) {
                resourceActionUpdatesStore.tryEmit(ResourceActionUpdate.ResourceEdited(result.resource))
            }
            appNavigator.back()
        }
    }

    private fun isOpenedFromEntryDetails(): Boolean {
        val stack = appNavigator.state.value.rootStack
        return stack.dropLast(1).lastOrNull() is EntryDetailsScreen
    }
}

internal fun buildEditComposerRequest(params: ResourceActionsParams): ComposerRequest? {
    val prefill = ComposerPrefill(
        content = params.content,
        adult = params.adult,
        photoKey = params.photoKey,
        photoUrl = params.photoUrl,
    )

    return when (params.resourceType) {
        ResourceActionsType.Entry -> ComposerRequest.EditEntry(
            entryId = params.rootId,
            prefill = prefill,
        )

        ResourceActionsType.EntryComment -> {
            val commentId = params.childId ?: return null
            ComposerRequest.EditEntryComment(
                entryId = params.rootId,
                commentId = commentId,
                prefill = prefill,
            )
        }

        ResourceActionsType.LinkComment -> {
            val commentId = params.childId ?: return null
            ComposerRequest.EditLinkComment(
                linkId = params.rootId,
                commentId = commentId,
                prefill = prefill,
            )
        }

        ResourceActionsType.Link -> null
    }
}

internal fun buildState(params: ResourceActionsParams): ResourceActionsBottomSheetState {
    val showVotersAction = ResourceActionItemState(
        id = ResourceActionId.ShowVoters,
        title = Res.string.resource_actions_show_voters,
        icon = Res.drawable.ic_add,
    )
    val showLinkUpvotersAction = ResourceActionItemState(
        id = ResourceActionId.ShowLinkUpvoters,
        title = Res.string.resource_actions_show_link_upvoters,
        icon = Res.drawable.ic_arrow_up,
    )
    val showLinkDownvotersAction = ResourceActionItemState(
        id = ResourceActionId.ShowLinkDownvoters,
        title = Res.string.resource_actions_show_link_downvoters,
        icon = Res.drawable.ic_arrow_down,
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
    val screenshotAction = params.screenshotDraftId?.let {
        ResourceActionItemState(
            id = ResourceActionId.ShareAsScreenshot,
            title = Res.string.resource_actions_share_as_screenshot,
            icon = Res.drawable.ic_share,
        )
    }
    val deleteEntryAction = params
        .takeIf {
            params.resourceType == ResourceActionsType.Entry && params.canDelete
        }
        ?.let {
            ResourceActionItemState(
                id = ResourceActionId.DeleteEntry,
                title = Res.string.resource_actions_delete_entry,
                icon = Res.drawable.ic_delete,
                isDestructive = true,
            )
        }
    val deleteEntryCommentAction = params
        .takeIf {
            params.resourceType == ResourceActionsType.EntryComment && params.canDelete
        }
        ?.let {
            ResourceActionItemState(
                id = ResourceActionId.DeleteEntryComment,
                title = Res.string.resource_actions_delete_entry_comment,
                icon = Res.drawable.ic_delete,
                isDestructive = true,
            )
        }
    val editEntryAction = params
        .takeIf {
            params.resourceType == ResourceActionsType.Entry && params.canEdit
        }
        ?.let {
            ResourceActionItemState(
                id = ResourceActionId.EditEntry,
                title = Res.string.resource_actions_edit_entry,
                icon = Res.drawable.ic_edit,
            )
        }
    val editEntryCommentAction = params
        .takeIf {
            params.resourceType == ResourceActionsType.EntryComment && params.canEdit
        }
        ?.let {
            ResourceActionItemState(
                id = ResourceActionId.EditEntryComment,
                title = Res.string.resource_actions_edit_comment,
                icon = Res.drawable.ic_edit,
            )
        }
    val editLinkCommentAction = params
        .takeIf {
            params.resourceType == ResourceActionsType.LinkComment && params.canEdit
        }
        ?.let {
            ResourceActionItemState(
                id = ResourceActionId.EditLinkComment,
                title = Res.string.resource_actions_edit_comment,
                icon = Res.drawable.ic_edit,
            )
        }

    return ResourceActionsBottomSheetState(
        actions = when (params.resourceType) {
            ResourceActionsType.Link -> listOfNotNull(
                showLinkUpvotersAction,
                showLinkDownvotersAction,
                copyLinkAction,
            ).toPersistentList()

            ResourceActionsType.Entry,
            -> listOfNotNull(
                showVotersAction,
                screenshotAction,
                copyLinkAction,
                editEntryAction,
                deleteEntryAction,
            ).toPersistentList()

            ResourceActionsType.EntryComment,
            -> listOfNotNull(
                showVotersAction,
                screenshotAction,
                copyLinkAction,
                editEntryCommentAction,
                deleteEntryCommentAction,
            ).toPersistentList()

            ResourceActionsType.LinkComment,
            -> listOfNotNull(
                screenshotAction,
                copyLinkAction,
                editLinkCommentAction,
            ).toPersistentList()
        },
    )
}

internal fun buildResourceLink(params: ResourceActionsParams): String = when (params.resourceType) {
    ResourceActionsType.Link -> {
        val slug = requireNotNull(params.rootSlug) { "Link actions require rootSlug" }
        "https://wykop.pl/link/${params.rootId}/$slug"
    }

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
