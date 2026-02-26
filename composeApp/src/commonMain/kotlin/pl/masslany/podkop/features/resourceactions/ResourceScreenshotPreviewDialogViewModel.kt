package pl.masslany.podkop.features.resourceactions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.ScreenshotExporter
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_generic_error
import podkop.composeapp.generated.resources.snackbar_image_saved
import podkop.composeapp.generated.resources.snackbar_screenshot_copied

class ResourceScreenshotPreviewDialogViewModel(
    private val draftId: String,
    private val draftStore: ResourceScreenshotShareDraftStore,
    private val appNavigator: AppNavigator,
    private val screenshotExporter: ScreenshotExporter,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ResourceScreenshotPreviewDialogActions {

    private val _state = MutableStateFlow(ResourceScreenshotPreviewDialogState.initial)
    val state = _state.asStateFlow()
    private val _snackbarEvents = MutableSharedFlow<SnackbarEvent>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    init {
        val draft = draftStore.get(draftId)
        if (draft == null) {
            snackbarManager.tryEmit(
                SnackbarEvent(
                    message = SnackbarMessage.Resource(Res.string.snackbar_generic_error),
                ),
            )
            appNavigator.back()
        } else {
            _state.update {
                ResourceScreenshotPreviewDialogState(
                    draft = draft,
                    showParent = draft.hasParent,
                    exportingAction = null,
                )
            }
        }
    }

    override fun onCancelClicked() {
        appNavigator.back()
    }

    override fun onShowParentChanged(showParent: Boolean) {
        _state.update { previous ->
            if (!previous.isParentToggleVisible) return@update previous
            previous.copy(showParent = showParent)
        }
    }

    override fun onScreenshotCaptured(
        image: ImageBitmap,
        action: ResourceScreenshotExportAction,
    ) {
        val draft = state.value.draft ?: return
        if (state.value.isExporting) return

        viewModelScope.launch {
            _state.update { it.copy(exportingAction = action) }

            val fileName = buildScreenshotFileName(draft)
            val isSuccess = runCatching {
                when (action) {
                    ResourceScreenshotExportAction.Copy -> screenshotExporter.copyToClipboard(
                        image = image,
                        fileName = fileName,
                    )

                    ResourceScreenshotExportAction.Share -> screenshotExporter.shareImage(
                        image = image,
                        fileName = fileName,
                    )

                    ResourceScreenshotExportAction.Save -> screenshotExporter.saveToGallery(
                        image = image,
                        fileName = fileName,
                    )
                }
            }.getOrDefault(false)

            when {
                !isSuccess -> {
                    _snackbarEvents.tryEmit(
                        SnackbarEvent(
                            message = SnackbarMessage.Resource(Res.string.snackbar_generic_error),
                        ),
                    )
                }

                action == ResourceScreenshotExportAction.Copy -> {
                    _snackbarEvents.tryEmit(
                        SnackbarEvent(
                            message = SnackbarMessage.Resource(Res.string.snackbar_screenshot_copied),
                        ),
                    )
                }

                action == ResourceScreenshotExportAction.Save -> {
                    _snackbarEvents.tryEmit(
                        SnackbarEvent(
                            message = SnackbarMessage.Resource(Res.string.snackbar_image_saved),
                        ),
                    )
                }
            }

            _state.update { it.copy(exportingAction = null) }

            if (isSuccess && action == ResourceScreenshotExportAction.Share) {
                appNavigator.back()
            }
        }
    }
}

private val ResourceScreenshotShareDraft.hasParent: Boolean
    get() = when (this) {
        is ResourceScreenshotShareDraft.Entry -> false
        is ResourceScreenshotShareDraft.EntryComment -> parentEntry != null
        is ResourceScreenshotShareDraft.LinkComment -> parentComment != null
    }

internal fun buildScreenshotFileName(draft: ResourceScreenshotShareDraft): String = when (draft) {
    is ResourceScreenshotShareDraft.Entry -> "entry_${draft.entry.id}"
    is ResourceScreenshotShareDraft.EntryComment -> "entry_comment_${draft.comment.id}"
    is ResourceScreenshotShareDraft.LinkComment -> "link_comment_${draft.comment.id}"
}
