package pl.masslany.podkop.features.settings.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.settings.SettingsScreenState

class SettingsScreenStateProvider : PreviewParameterProvider<SettingsScreenState> {
    override val values: Sequence<SettingsScreenState> = sequenceOf(
        SettingsScreenState.initial,
        SettingsScreenState(
            autoplayGifs = true,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            supportsDynamicColorsToggle = true,
            showDebugTools = true,
            showLogoutButton = true,
        ),
        SettingsScreenState(
            autoplayGifs = false,
            themeOverride = ThemeOverride.DARK,
            dynamicColorsEnabled = false,
            supportsDynamicColorsToggle = true,
            showDebugTools = false,
            showLogoutButton = true,
        ),
    )
}
