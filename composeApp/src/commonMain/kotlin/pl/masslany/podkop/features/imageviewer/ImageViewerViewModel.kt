package pl.masslany.podkop.features.imageviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_generic_error
import podkop.composeapp.generated.resources.snackbar_image_saved
import podkop.composeapp.generated.resources.snackbar_screenshot_copied

class ImageViewerViewModel(
    imageUrl: String,
    private val appNavigator: AppNavigator,
    private val imageExportService: ImageExportService,
    private val snackbarManager: SnackbarManager,
    private val dismissAfterAction: Boolean = false,
) : ViewModel(),
    ImageViewerActions {

    private val _state = MutableStateFlow(ImageViewerScreenState(imageUrl = imageUrl))
    val state = _state.asStateFlow()

    override fun onBackClicked() {
        appNavigator.back()
    }

    override fun onDownloadClicked(url: String) {
        if (_state.value.isCopying) return
        val success = runCatching { imageExportService.downloadImage(url) }.getOrDefault(false)
        showResult(success, Res.string.snackbar_image_saved)
    }

    override fun onCopyClicked(url: String) {
        if (_state.value.isCopying) return
        _state.update { it.copy(isCopying = true) }
        viewModelScope.launch {
            try {
                val success = imageExportService.copyImage(url)
                showResult(success, Res.string.snackbar_screenshot_copied)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showResult(false, Res.string.snackbar_screenshot_copied)
            } finally {
                _state.update { it.copy(isCopying = false) }
            }
        }
    }

    private fun showResult(success: Boolean, successMessage: StringResource) {
        if (dismissAfterAction) appNavigator.back()
        snackbarManager.tryEmit(
            SnackbarEvent(
                message = SnackbarMessage.Resource(
                    if (success) successMessage else Res.string.snackbar_generic_error,
                ),
                isFinite = true,
            ),
        )
    }
}
