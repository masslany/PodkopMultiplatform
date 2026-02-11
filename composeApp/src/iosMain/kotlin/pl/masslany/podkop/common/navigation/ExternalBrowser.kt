package pl.masslany.podkop.common.navigation

import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual class ExternalBrowser(private val viewControllerProvider: () -> UIViewController?) {

    actual fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val rootVC = viewControllerProvider() ?: return

        try {
            val safariVC = SFSafariViewController(nsUrl)
            rootVC.presentViewController(
                safariVC,
                animated = true,
                completion = null,
            )
        } catch (_: Throwable) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
