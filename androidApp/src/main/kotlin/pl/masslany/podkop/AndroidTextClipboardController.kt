package pl.masslany.podkop

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import pl.masslany.podkop.common.platform.TextClipboardController

class AndroidTextClipboardController(
    application: Application,
) : TextClipboardController {
    private val clipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override suspend fun setText(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("podkop", text))
    }
}
