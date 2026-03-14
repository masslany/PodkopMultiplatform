package pl.masslany.podkop.common.platform

interface TextClipboardController {
    suspend fun setText(text: String)
}
