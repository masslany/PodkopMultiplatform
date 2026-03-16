package pl.masslany.podkop.features.privatemessages.preview

import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.features.privatemessages.conversation.ConversationActions

object NoOpConversationActions : ConversationActions {
    override fun onTopBarBackClicked() = Unit

    override fun onRefresh() = Unit

    override fun onRetryClicked() = Unit

    override fun onComposerTextChanged(content: TextFieldValue) = Unit

    override fun onComposerAdultChanged(adult: Boolean) = Unit

    override fun onComposerPhotoAttachClicked() = Unit

    override fun onComposerPhotoRemoved() = Unit

    override fun onComposerSubmit() = Unit

    override fun onProfileClicked(username: String) = Unit

    override fun onTagClicked(tag: String) = Unit

    override fun onUrlClicked(url: String) = Unit

    override fun onImageClicked(url: String) = Unit
}
