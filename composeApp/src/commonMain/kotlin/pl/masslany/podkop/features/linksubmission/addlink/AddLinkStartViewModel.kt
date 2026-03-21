package pl.masslany.podkop.features.linksubmission.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.linksubmission.LinkDraftScreen
import pl.masslany.podkop.features.linksubmission.linkdraft.toAddLinkSimilarItemState
import pl.masslany.podkop.features.linksubmission.linkdraft.toSavedDraftState
import pl.masslany.podkop.features.topbar.TopBarActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_delete
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.dialog_title_delete_link_draft
import podkop.composeapp.generated.resources.snackbar_add_link_draft_deleted

internal class AddLinkStartViewModel(
    private val linksRepository: LinksRepository,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val topBarActions: TopBarActions,
) : ViewModel(),
    AddLinkStartActions,
    TopBarActions by topBarActions {

    private val _state = MutableStateFlow(AddLinkStartState.initial)
    val state = _state.asStateFlow()

    fun onScreenOpened() {
        loadDrafts()
    }

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onUrlChanged(value: String) {
        _state.update { previous ->
            val normalized = value.trim()
            val shouldResetDraftState = previous.draftedUrl != null && normalized != previous.draftedUrl

            previous.copy(
                url = value,
                draftedUrl = if (shouldResetDraftState) null else previous.draftedUrl,
                draftKey = if (shouldResetDraftState) null else previous.draftKey,
                similarLinks = if (shouldResetDraftState) persistentListOf() else previous.similarLinks,
                showUrlFormatError = false,
            )
        }
    }

    override fun onContinueClicked() {
        val currentState = state.value
        if (!currentState.canContinue) {
            return
        }

        val normalizedUrl = currentState.url.trim()
        if (!isHttpUrl(normalizedUrl)) {
            _state.update { it.copy(showUrlFormatError = true) }
            return
        }

        if (normalizedUrl == currentState.draftedUrl && currentState.draftKey != null && currentState.hasSimilarLinks) {
            appNavigator.navigateTo(LinkDraftScreen(draftKey = currentState.draftKey))
            return
        }

        _state.update { it.copy(isCheckingDraft = true, showUrlFormatError = false) }
        viewModelScope.launch {
            linksRepository.createLinkDraft(normalizedUrl)
                .onSuccess { draft ->
                    val similarLinks = draft.similar
                        .map { it.toAddLinkSimilarItemState() }
                        .toImmutableList()

                    _state.update {
                        it.copy(
                            draftedUrl = normalizedUrl,
                            draftKey = draft.key,
                            similarLinks = similarLinks,
                            isCheckingDraft = false,
                        )
                    }

                    if (draft.duplicate || similarLinks.isNotEmpty()) {
                        return@onSuccess
                    }

                    _state.update {
                        it.copy(
                            draftedUrl = null,
                            draftKey = null,
                            similarLinks = persistentListOf(),
                        )
                    }
                    appNavigator.navigateTo(LinkDraftScreen(draftKey = draft.key))
                }
                .onFailure {
                    logger.error("Failed to create add-link draft for url=$normalizedUrl", it)
                    _state.update { previous -> previous.copy(isCheckingDraft = false) }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    override fun onSavedDraftContinueClicked(key: String) {
        if (state.value.deletingDraftKey != null) {
            return
        }
        appNavigator.navigateTo(LinkDraftScreen(draftKey = key))
    }

    override fun onSavedDraftDeleteClicked(key: String) {
        if (state.value.deletingDraftKey != null) {
            return
        }

        viewModelScope.launch {
            val dialog = GenericDialog.fromResources(
                title = Res.string.dialog_title_delete_link_draft,
                positiveText = Res.string.dialog_button_delete,
                negativeText = Res.string.dialog_button_dismiss,
            )
            val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
            if (!confirmed) {
                return@launch
            }

            _state.update { it.copy(deletingDraftKey = key) }
            linksRepository.deleteLinkDraft(key)
                .onSuccess {
                    _state.update { previous ->
                        previous.copy(
                            currentDrafts = previous.currentDrafts.filterNot { it.key == key }.toImmutableList(),
                            draftedUrl = if (previous.draftKey == key) null else previous.draftedUrl,
                            draftKey = if (previous.draftKey == key) null else previous.draftKey,
                            similarLinks = if (previous.draftKey == key) persistentListOf() else previous.similarLinks,
                            deletingDraftKey = null,
                        )
                    }
                    snackbarManager.tryEmit(
                        SnackbarEvent(
                            message = SnackbarMessage.Resource(Res.string.snackbar_add_link_draft_deleted),
                        ),
                    )
                }
                .onFailure {
                    logger.error("Failed to delete add-link draft key=$key", it)
                    _state.update { previous -> previous.copy(deletingDraftKey = null) }
                    snackbarManager.tryEmitGenericError()
                }
        }
    }

    private fun loadDrafts() {
        _state.update { it.copy(isLoadingDrafts = true) }
        viewModelScope.launch {
            linksRepository.getLinkDrafts()
                .onSuccess { drafts ->
                    _state.update {
                        it.copy(
                            currentDrafts = drafts.map { draft -> draft.toSavedDraftState() }.toImmutableList(),
                            isLoadingDrafts = false,
                            deletingDraftKey = null,
                        )
                    }
                }
                .onFailure {
                    logger.error("Failed to load add-link drafts", it)
                    _state.update { previous -> previous.copy(isLoadingDrafts = false) }
                }
        }
    }

    private fun isHttpUrl(value: String): Boolean {
        val normalized = value.lowercase()
        return normalized.startsWith("http://") || normalized.startsWith("https://")
    }
}
