package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogActions

object NoOpResourceTextSelectionDialogActions : ResourceTextSelectionDialogActions {
    override fun onTextChanged(content: TextFieldValue) = Unit

    override fun onCopySelectionCompleted() = Unit

    override fun onDismissClicked() = Unit
}
