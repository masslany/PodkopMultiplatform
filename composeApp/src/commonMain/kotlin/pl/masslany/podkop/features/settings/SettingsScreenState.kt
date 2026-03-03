package pl.masslany.podkop.features.settings

import pl.masslany.podkop.common.settings.ThemeOverride

data class SettingsScreenState(
    val autoplayGifs: Boolean?,
    val themeOverride: ThemeOverride,
    val dynamicColorsEnabled: Boolean,
    val supportsDynamicColorsToggle: Boolean,
    val showDebugTools: Boolean,
    val showLogoutButton: Boolean,
    val appVersion: String,
) {
    companion object {
        val initial = SettingsScreenState(
            autoplayGifs = null,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            supportsDynamicColorsToggle = false,
            showDebugTools = false,
            showLogoutButton = false,
            appVersion = "",
        )
    }
}
