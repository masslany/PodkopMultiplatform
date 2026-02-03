package pl.masslany.podkop.common.models

sealed class EntryContentState {

    data class Content(
        val content: String,
    ) : EntryContentState()

    data object DeletedByModerator : EntryContentState()

    data object DeletedByAuthor : EntryContentState()
}
