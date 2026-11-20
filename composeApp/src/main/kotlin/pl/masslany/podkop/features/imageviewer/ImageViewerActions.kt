package pl.masslany.podkop.features.imageviewer

interface ImageViewerActions {
    fun onBackClicked()

    fun onCopyClicked(url: String)

    fun onDownloadClicked(url: String)
}
