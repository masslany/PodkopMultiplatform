package pl.masslany.podkop.widget

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import pl.masslany.podkop.MainActivity

internal fun openHitsIntent(context: Context): Intent = appIntent(context, HITS_DEEP_LINK)

internal fun openLinkIntent(context: Context, linkId: Int): Intent =
    appIntent(context, LINK_DEEP_LINK_PREFIX + linkId)

/** Opens [deepLink] in the app through the same handler as notifications and app links. */
private fun appIntent(context: Context, deepLink: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = deepLink.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private const val HITS_DEEP_LINK = "https://masslany.pl/app/hits"
private const val LINK_DEEP_LINK_PREFIX = "https://masslany.pl/wykop/link/"
