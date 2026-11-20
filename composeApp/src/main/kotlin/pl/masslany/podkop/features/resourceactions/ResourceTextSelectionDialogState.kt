package pl.masslany.podkop.features.resourceactions

import androidx.compose.ui.text.input.TextFieldValue

data class ResourceTextSelectionDialogState(
    val content: TextFieldValue,
    val previewDraft: ResourceScreenshotShareDraft? = null,
) {
    companion object {
        val initial = ResourceTextSelectionDialogState(
            content = TextFieldValue(),
        )
    }

    val hasSelection: Boolean
        get() = !content.selection.collapsed
}
