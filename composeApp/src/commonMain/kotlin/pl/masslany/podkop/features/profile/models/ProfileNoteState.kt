package pl.masslany.podkop.features.profile.models

data class ProfileNoteState(
    val content: String,
    val savedContent: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isSaving: Boolean,
    val hasLoaded: Boolean,
) {
    val canSave: Boolean
        get() = !isLoading && !isSaving && content != savedContent

    companion object Companion {
        val initial = ProfileNoteState(
            content = "",
            savedContent = "",
            isLoading = false,
            isError = false,
            isSaving = false,
            hasLoaded = false,
        )
    }
}
