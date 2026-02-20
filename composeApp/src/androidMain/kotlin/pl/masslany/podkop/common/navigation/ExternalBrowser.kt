package pl.masslany.podkop.common.navigation

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import pl.masslany.podkop.common.logging.api.AppLogger

actual class ExternalBrowser(
    private val activityProvider: () -> Activity?,
    private val application: Application,
    private val logger: AppLogger,
) {

    actual fun open(url: String) {
        val uri = url.toUri()

        val activity = activityProvider()

        if (activity != null) {
            try {
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(activity, uri)
                return
            } catch (e: Exception) {
                logger.warn("Custom tabs failed, falling back to default browser", e)
            }
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            application.startActivity(intent)
        } catch (e: Exception) {
            logger.error("application.startActivity failed for url=$url", e)
        }
    }
}
