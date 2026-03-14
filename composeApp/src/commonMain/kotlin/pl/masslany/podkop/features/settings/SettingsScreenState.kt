package pl.masslany.podkop.features.settings

import pl.masslany.podkop.common.settings.ThemeOverride

data class SettingsScreenState(
    val isLoading: Boolean,
    val privateMessagesBackgroundNotificationsEnabled: Boolean,
    val supportsPrivateMessagesBackgroundNotifications: Boolean,
    val areSystemNotificationsEnabled: Boolean,
    val shouldRequestNotificationPermission: Boolean,
    val autoplayGifs: Boolean,
    val themeOverride: ThemeOverride,
    val dynamicColorsEnabled: Boolean,
    val supportsDynamicColorsToggle: Boolean,
    val analyticsEnabled: Boolean,
    val crashReportingEnabled: Boolean,
    val supportsTelemetryControls: Boolean,
    val supportsCacheClearing: Boolean,
    val showDebugTools: Boolean,
    val showLogoutButton: Boolean,
    val appVersion: String,
) {
    companion object {
        val initial = SettingsScreenState(
            isLoading = true,
            privateMessagesBackgroundNotificationsEnabled = false,
            supportsPrivateMessagesBackgroundNotifications = false,
            areSystemNotificationsEnabled = false,
            shouldRequestNotificationPermission = false,
            autoplayGifs = true,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            supportsDynamicColorsToggle = false,
            analyticsEnabled = false,
            crashReportingEnabled = false,
            supportsTelemetryControls = false,
            supportsCacheClearing = false,
            showDebugTools = false,
            showLogoutButton = false,
            appVersion = "",
        )
    }
}
