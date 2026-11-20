package pl.masslany.podkop.common.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import pl.masslany.podkop.common.settings.LocalAppSettings
import pl.masslany.podkop.common.theme.PodkopTheme

@Composable
internal fun PodkopPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    PodkopTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
    ) {
        val appSettings = remember { FakeAppSettings() }
        CompositionLocalProvider(
            LocalAppSettings provides appSettings,
        ) {
            Surface {
                content()
            }
        }
    }
}
