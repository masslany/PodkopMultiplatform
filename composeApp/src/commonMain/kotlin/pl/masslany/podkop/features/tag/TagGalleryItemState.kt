package pl.masslany.podkop.features.tag

data class TagGalleryItemState(
    val resourceIndex: Int,
    val entryId: Int,
    val imageUrl: String,
    val aspectRatio: Float?,
    val isAdult: Boolean,
    val isGif: Boolean,
) {
    val galleryAspectRatio: Float
        get() = aspectRatio
            ?.coerceIn(TAG_GALLERY_MIN_ASPECT_RATIO, TAG_GALLERY_MAX_ASPECT_RATIO)
            ?: 1f

    companion object {
        private const val TAG_GALLERY_MIN_ASPECT_RATIO = 0.65f
        private const val TAG_GALLERY_MAX_ASPECT_RATIO = 1.8f
    }
}
