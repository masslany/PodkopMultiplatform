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
            showPrivateMessagesBackgroundNotifications = true,
            areSystemNotificationsEnabled = true,
            shouldRequestNotificationPermission = false,
            autoplayGifs = true,
            playVideosInline = false,
            threadedEntryComments = false,
            themeOverride = ThemeOverride.AUTO,
            dynamicColorsEnabled = true,
            analyticsEnabled = true,
            crashReportingEnabled = true,
            showDebugTools = true,
            showAccountSection = true,
            appVersion = "2.0.0-debug",
        ),
        SettingsScreenState(
            isLoading = false,
            privateMessagesBackgroundNotificationsEnabled = false,
            showPrivateMessagesBackgroundNotifications = true,
            areSystemNotificationsEnabled = false,
            shouldRequestNotificationPermission = false,
            autoplayGifs = false,
            playVideosInline = false,
            threadedEntryComments = false,
            themeOverride = ThemeOverride.DARK,
            dynamicColorsEnabled = false,
            analyticsEnabled = false,
            crashReportingEnabled = true,
            showDebugTools = false,
            showAccountSection = true,
            appVersion = "2.0.0",
        ),
    )
}
