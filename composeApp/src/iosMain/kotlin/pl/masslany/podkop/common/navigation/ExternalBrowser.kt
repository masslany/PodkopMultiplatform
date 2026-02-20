package pl.masslany.podkop.common.navigation

import pl.masslany.podkop.common.logging.api.AppLogger
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual class ExternalBrowser(
    private val viewControllerProvider: () -> UIViewController?,
    private val logger: AppLogger,
) {

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
        } catch (throwable: Throwable) {
            logger.warn(
                "SFSafariViewController failed, falling back to UIApplication.openURL",
                throwable,
            )
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
