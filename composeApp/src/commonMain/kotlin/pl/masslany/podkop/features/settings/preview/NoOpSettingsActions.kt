package pl.masslany.podkop.features.settings.preview

import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.settings.SettingsActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpSettingsActions : SettingsActions, TopBarActions by NoOpTopBarActions {
    override fun onPrivateMessagesBackgroundNotificationsChanged(enabled: Boolean) = Unit
    override fun onAutoplayGifsChanged(enabled: Boolean) = Unit
    override fun onThemeOverrideChanged(value: ThemeOverride) = Unit
    override fun onDynamicColorsChanged(enabled: Boolean) = Unit
    override fun onNotificationPermissionResult(granted: Boolean) = Unit
    override fun onAnalyticsCollectionChanged(enabled: Boolean) = Unit
    override fun onCrashReportingChanged(enabled: Boolean) = Unit
    override fun onClearCacheClicked() = Unit
    override fun onCopyDiagnosticsClicked() = Unit
    override fun onDebugToolsClicked() = Unit
    override fun onManageBlacklistsClicked() = Unit
    override fun onLogoutClicked() = Unit
}
