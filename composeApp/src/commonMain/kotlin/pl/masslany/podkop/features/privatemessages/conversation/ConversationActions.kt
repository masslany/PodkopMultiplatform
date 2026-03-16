package pl.masslany.podkop.features.privatemessages.conversation

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface ConversationActions : TopBarActions {
    fun onRefresh()

    fun onRetryClicked()

    fun onComposerTextChanged(content: TextFieldValue)

    fun onComposerAdultChanged(adult: Boolean)

    fun onComposerPhotoAttachClicked()

    fun onComposerPhotoRemoved()

    fun onComposerSubmit()

    fun onProfileClicked(username: String)

    fun onTagClicked(tag: String)

    fun onUrlClicked(url: String)

    fun onImageClicked(url: String)
}
