package pl.masslany.podkop.common.platform

import platform.UIKit.UIPasteboard

class IOSTextClipboardController : TextClipboardController {
    override suspend fun setText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
