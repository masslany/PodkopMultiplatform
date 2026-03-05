package pl.masslany.podkop.features.composer

import androidx.compose.ui.text.input.TextFieldValue

interface ComposerBottomSheetActions {
    fun onComposerTextChanged(content: TextFieldValue)

    fun onComposerAdultChanged(adult: Boolean)

    fun onComposerPhotoAttachClicked()

    fun onComposerPhotoRemoved()

    fun onComposerDismissed()

    fun onComposerSubmit()
}
