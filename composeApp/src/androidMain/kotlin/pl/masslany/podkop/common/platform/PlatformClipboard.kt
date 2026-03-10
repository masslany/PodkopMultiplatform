package pl.masslany.podkop.common.platform

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry

actual class PlatformClipboard(private val clipboard: Clipboard) {
    actual suspend fun setText(text: String) {
        clipboard.setClipEntry(ClipData.newPlainText("podkop", text).toClipEntry())
    }
}

@Composable
actual fun rememberPlatformClipboard(): PlatformClipboard {
    val clipboard = LocalClipboard.current
    return remember(clipboard) {
        PlatformClipboard(clipboard = clipboard)
    }
}
