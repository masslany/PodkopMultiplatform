package pl.masslany.podkop.features.linkdetails

import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

data class LinkDetailsScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isLoggedIn: Boolean,
    val currentUsername: String?,
    val link: LinkItemState?,
    val commentsState: LinkDetailsCommentsState,
    val relatedState: LinkDetailsRelatedState,
    val isComposerVisible: Boolean,
    val composerContent: TextFieldValue,
    val composerReplyTarget: String?,
    val composerParentCommentId: Int?,
    val composerAdult: Boolean,
    val isComposerSubmitting: Boolean,
) {
    companion object Companion {
        val initial = LinkDetailsScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isLoggedIn = false,
            currentUsername = null,
            link = null,
            commentsState = LinkDetailsCommentsState.Loading(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Loading,
            isComposerVisible = false,
            composerContent = TextFieldValue(),
            composerReplyTarget = null,
            composerParentCommentId = null,
            composerAdult = false,
            isComposerSubmitting = false,
        )
    }
}
