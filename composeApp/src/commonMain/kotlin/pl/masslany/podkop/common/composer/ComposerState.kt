package pl.masslany.podkop.common.composer

import androidx.compose.ui.text.input.TextFieldValue

data class ComposerState(
    val isVisible: Boolean,
    val content: TextFieldValue,
    val replyTarget: String?,
    val parentCommentId: Int?,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
    val isSubmitting: Boolean,
    val isMediaUploading: Boolean,
) {
    companion object {
        val initial = ComposerState(
            isVisible = false,
            content = TextFieldValue(),
            replyTarget = null,
            parentCommentId = null,
            adult = false,
            photoKey = null,
            photoUrl = null,
            isSubmitting = false,
            isMediaUploading = false,
        )
    }
}
