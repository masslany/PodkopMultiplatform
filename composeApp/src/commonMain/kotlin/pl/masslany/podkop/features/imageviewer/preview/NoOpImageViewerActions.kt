package pl.masslany.podkop.features.imageviewer.preview

import pl.masslany.podkop.features.imageviewer.ImageViewerActions

object NoOpImageViewerActions : ImageViewerActions {
    override fun onBackClicked() = Unit
    override fun onDownloadClicked(url: String) = Unit
}
