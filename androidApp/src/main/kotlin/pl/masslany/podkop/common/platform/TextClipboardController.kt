package pl.masslany.podkop.common.platform

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class TextClipboardController(application: Application) {
    private val clipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    suspend fun setText(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("podkop", text))
    }
}
