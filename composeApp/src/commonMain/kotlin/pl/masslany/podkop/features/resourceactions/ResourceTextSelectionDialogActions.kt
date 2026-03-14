package pl.masslany.podkop.features.resourceactions

import androidx.compose.ui.text.input.TextFieldValue

interface ResourceTextSelectionDialogActions {
    fun onTextChanged(content: TextFieldValue)

    fun onCopySelectionCompleted()

    fun onDismissClicked()
}
