package pl.masslany.podkop.features.settings

import pl.masslany.podkop.common.settings.ThemeOverride

data class SettingsScreenState(
    val isLoading: Boolean,
    val privateMessagesBackgroundNotificationsEnabled: Boolean,
    val showPrivateMessagesBackgroundNotifications: Boolean,
    val areSystemNotificationsEnabled: Boolean,
    val shouldRequestNotificationPermission: Boolean,
    val autoplayGifs: Boolean,
    val playVideosInline: Boolean,
    val threadedEntryComments: Boolean,
    val themeOverride: ThemeOverride,
    val dynamicColorsEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val crashReportingEnabled: Boolean,
    val showDebugTools: Boolean,
    val showAccountSection: Boolean,
    val appVersion: String,
) {
    companion object {
        val initial = SettingsScreenState(
            isLoading = true,
            privateMessagesBackgroundNotificationsEnabled = false,
            showPrivateMessagesBackgroundNotifications = false,
            areSystemNotificationsEnabled = false,
            shouldRequestNotificationPermission = false,
            autoplayGifs = true,
            playVideosInline = true,
            threadedEntryComments = false,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            analyticsEnabled = false,
            crashReportingEnabled = false,
            showDebugTools = false,
            showAccountSection = false,
            appVersion = "",
        )
    }
}
