package pl.masslany.podkop.features.resourceactions

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_text_copied

class ResourceTextSelectionDialogViewModel(
    content: String,
    private val previewDraftId: String?,
    private val draftStore: ResourceScreenshotShareDraftStore,
    private val appNavigator: AppNavigator,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ResourceTextSelectionDialogActions {

    private val _state = MutableStateFlow(
        ResourceTextSelectionDialogState(
            content = TextFieldValue(text = content),
            previewDraft = previewDraftId?.let { draftId ->
                draftStore.get(draftId)
            },
        ),
    )
    val state = _state.asStateFlow()

    override fun onTextChanged(content: TextFieldValue) {
        _state.update { previous ->
            previous.copy(content = content)
        }
    }

    override fun onCopySelectionCompleted() {
        snackbarManager.tryEmit(
            SnackbarEvent(
                message = SnackbarMessage.Resource(Res.string.snackbar_text_copied),
            ),
        )
        appNavigator.back()
    }

    override fun onDismissClicked() {
        appNavigator.back()
    }

    override fun onCleared() {
        previewDraftId?.let { draftId ->
            draftStore.remove(draftId)
        }
        super.onCleared()
    }
}
