package pl.masslany.podkop.features.settings

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface SettingsActions : TopBarActions {
    fun onPrivateMessagesBackgroundNotificationsChanged(enabled: Boolean)

    fun onAutoplayGifsChanged(enabled: Boolean)

    fun onThemeOverrideChanged(value: ThemeOverride)

    fun onDynamicColorsChanged(enabled: Boolean)

    fun onNotificationPermissionResult(granted: Boolean)

    fun onAnalyticsCollectionChanged(enabled: Boolean)

    fun onCrashReportingChanged(enabled: Boolean)

    fun onClearCacheClicked()

    fun onCopyDiagnosticsClicked()

    fun onDebugToolsClicked()

    fun onManageBlacklistsClicked()

    fun onLogoutClicked()
}
