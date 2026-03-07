package pl.masslany.podkop.features.privatemessages.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.composer.ComposerState

data class ConversationScreenState(
    val username: String,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val scrollToLatestMessage: Int,
    val messages: ImmutableList<ConversationMessageItemState>,
    val composer: ComposerState,
) {
    val canSubmit: Boolean =
        !composer.isSubmitting &&
            !composer.isMediaUploading &&
            (composer.content.text.isNotBlank() || composer.photoKey != null)

    companion object {
        fun initial(username: String): ConversationScreenState = ConversationScreenState(
            username = username,
            isLoading = true,
            isRefreshing = false,
            isError = false,
            isPaginating = false,
            scrollToLatestMessage = 0,
            messages = persistentListOf(),
            composer = ComposerState.initial,
        )
    }
}
