package pl.masslany.podkop.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as MaterialColorProviders
import pl.masslany.podkop.common.theme.darkColorScheme
import pl.masslany.podkop.common.theme.lightColorScheme

/** Dynamic colors where the system has them, the app's own palette before Android 12. */
@Composable
internal fun hitsWidgetColors(): ColorProviders = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceTheme.colors
} else {
    MaterialColorProviders(light = lightColorScheme, dark = darkColorScheme)
}
