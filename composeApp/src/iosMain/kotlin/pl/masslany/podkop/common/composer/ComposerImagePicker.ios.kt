package pl.masslany.podkop.common.composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberComposerImagePickerLauncher(
    onImagePicked: (ComposerPickedImage) -> Unit,
    onCancelled: () -> Unit,
): ComposerImagePickerLauncher = remember {
    object : ComposerImagePickerLauncher {
        override val isAvailable: Boolean = false

        override fun launch() = Unit
    }
}

internal actual fun isComposerImagePickerAvailable(): Boolean = false
