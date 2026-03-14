package pl.masslany.podkop.features.settings.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.settings.SettingsScreenState

class SettingsScreenStateProvider : PreviewParameterProvider<SettingsScreenState> {
    override val values: Sequence<SettingsScreenState> = sequenceOf(
        SettingsScreenState.initial,
        SettingsScreenState(
            isLoading = false,
            privateMessagesBackgroundNotificationsEnabled = true,
            supportsPrivateMessagesBackgroundNotifications = true,
            areSystemNotificationsEnabled = true,
            shouldRequestNotificationPermission = false,
            autoplayGifs = true,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            supportsDynamicColorsToggle = true,
            analyticsEnabled = true,
            crashReportingEnabled = true,
            supportsTelemetryControls = true,
            supportsCacheClearing = true,
            showDebugTools = true,
            showLogoutButton = true,
            appVersion = "2.0.0-debug",
        ),
        SettingsScreenState(
            isLoading = false,
            privateMessagesBackgroundNotificationsEnabled = false,
            supportsPrivateMessagesBackgroundNotifications = true,
            areSystemNotificationsEnabled = false,
            shouldRequestNotificationPermission = false,
            autoplayGifs = false,
            themeOverride = ThemeOverride.DARK,
            dynamicColorsEnabled = false,
            supportsDynamicColorsToggle = true,
            analyticsEnabled = false,
            crashReportingEnabled = true,
            supportsTelemetryControls = true,
            supportsCacheClearing = true,
            showDebugTools = false,
            showLogoutButton = true,
            appVersion = "2.0.0",
        ),
    )
}
