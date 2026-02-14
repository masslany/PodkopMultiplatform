package pl.masslany.podkop.features.imageviewer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.ImageDownloader
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_generic_error
import podkop.composeapp.generated.resources.snackbar_image_saved

class ImageViewerViewModel(
    imageUrl: String,
    private val appNavigator: AppNavigator,
    private val imageDownloader: ImageDownloader,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ImageViewerActions {

    private val _state = MutableStateFlow(ImageViewerScreenState(imageUrl = imageUrl))
    val state = _state.asStateFlow()

    override fun onBackClicked() {
        appNavigator.back()
    }

    override fun onDownloadClicked(url: String) {
        val event = if (imageDownloader.downloadImage(url)) {
            SnackbarEvent(
                message = SnackbarMessage.Resource(Res.string.snackbar_image_saved),
                isFinite = true,
            )
        } else {
            SnackbarEvent(
                message = SnackbarMessage.Resource(Res.string.snackbar_generic_error),
                isFinite = true,
            )
        }
        snackbarManager.tryEmit(event)
    }
}
