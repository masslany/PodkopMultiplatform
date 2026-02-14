package pl.masslany.podkop.common.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class ImageDownloader {

    actual fun downloadImage(url: String): Boolean {
        val imageUrl = NSURL.URLWithString(url) ?: return false
        return runCatching {
            UIApplication.sharedApplication.openURL(imageUrl)
        }.isSuccess
    }
}
