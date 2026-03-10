package pl.masslany.podkop.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard

actual class PlatformClipboard(private val clipboard: Clipboard) {
    @OptIn(ExperimentalComposeUiApi::class)
    actual suspend fun setText(text: String) {
        clipboard.setClipEntry(ClipEntry.withPlainText(text))
    }
}

@Composable
actual fun rememberPlatformClipboard(): PlatformClipboard {
    val clipboard = LocalClipboard.current
    return remember(clipboard) {
        PlatformClipboard(clipboard = clipboard)
    }
}
