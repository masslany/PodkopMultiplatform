package pl.masslany.podkop.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.common.deeplink.AuthSessionEvent
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.platform.AppMaintenanceController
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.common.platform.TextClipboardController
import pl.masslany.podkop.common.platform.supportsDynamicColorsToggle
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.TelemetrySettingsController
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.blacklists.BlacklistsScreen
import pl.masslany.podkop.features.debug.DebugScreen
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesBackgroundNotificationsController
import pl.masslany.podkop.features.topbar.TopBarActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_confirm
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.dialog_title_logout
import podkop.composeapp.generated.resources.settings_snackbar_cache_cleared
import podkop.composeapp.generated.resources.settings_snackbar_diagnostics_copied
import podkop.composeapp.generated.resources.settings_snackbar_notifications_permission_required

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val appSettings: AppSettings,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    private val buildInfo: BuildInfo,
    private val privateMessagesBackgroundNotificationsController: PrivateMessagesBackgroundNotificationsController,
    private val telemetrySettingsController: TelemetrySettingsController,
    private val appMaintenanceController: AppMaintenanceController,
    private val textClipboardController: TextClipboardController,
    topBarActions: TopBarActions,
) : ViewModel(),
    SettingsActions,
    TopBarActions by topBarActions {
    private val isLoggedIn = MutableStateFlow(false)
    private val shouldRequestNotificationPermission = MutableStateFlow(false)
    private val visualPreferencesState = combine(
        appSettings.autoplayGifs,
        appSettings.themeOverride,
        appSettings.dynamicColorsEnabled,
    ) { autoplayGifs, themeOverride, dynamicColorsEnabled ->
        VisualPreferencesState(
            autoplayGifs = autoplayGifs,
            themeOverride = themeOverride,
            dynamicColorsEnabled = dynamicColorsEnabled,
        )
    }
    private val telemetryPreferencesState = combine(
        telemetrySettingsController.analyticsEnabled,
        telemetrySettingsController.crashReportingEnabled,
    ) { analyticsEnabled, crashReportingEnabled ->
        TelemetryPreferencesState(
            analyticsEnabled = analyticsEnabled,
            crashReportingEnabled = crashReportingEnabled,
        )
    }
    private val preferencesState = combine(
        visualPreferencesState,
        telemetryPreferencesState,
    ) { visualPreferencesState, telemetryPreferencesState ->
        SettingsPreferencesState(
            autoplayGifs = visualPreferencesState.autoplayGifs,
            themeOverride = visualPreferencesState.themeOverride,
            dynamicColorsEnabled = visualPreferencesState.dynamicColorsEnabled,
            analyticsEnabled = telemetryPreferencesState.analyticsEnabled,
            crashReportingEnabled = telemetryPreferencesState.crashReportingEnabled,
        )
    }

    val state = combine(
        privateMessagesBackgroundNotificationsController.backgroundNotificationsEnabled,
        preferencesState,
        isLoggedIn,
        shouldRequestNotificationPermission,
    ) { backgroundNotificationsEnabled, preferencesState, isLoggedIn, shouldRequestNotificationPermission ->
        SettingsScreenState(
            isLoading = false,
            privateMessagesBackgroundNotificationsEnabled = backgroundNotificationsEnabled,
            supportsPrivateMessagesBackgroundNotifications =
                privateMessagesBackgroundNotificationsController.supportsSettings &&
                    isLoggedIn,
            areSystemNotificationsEnabled =
                privateMessagesBackgroundNotificationsController.areSystemNotificationsEnabled(),
            shouldRequestNotificationPermission = shouldRequestNotificationPermission,
            autoplayGifs = preferencesState.autoplayGifs,
            themeOverride = preferencesState.themeOverride,
            dynamicColorsEnabled = preferencesState.dynamicColorsEnabled,
            supportsDynamicColorsToggle = supportsDynamicColorsToggle(),
            analyticsEnabled = preferencesState.analyticsEnabled,
            crashReportingEnabled = preferencesState.crashReportingEnabled,
            supportsTelemetryControls = telemetrySettingsController.supportsControls,
            supportsCacheClearing = appMaintenanceController.supportsCacheClearing,
            showDebugTools = buildInfo.isDebugBuild,
            showAccountSection = isLoggedIn,
            appVersion = buildInfo.appVersionName,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = SettingsScreenState.initial,
        )

    init {
        refreshLoggedInState()
        observeAuthSessionChanges()
    }

    override fun onPrivateMessagesBackgroundNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                shouldRequestNotificationPermission.value = false
                privateMessagesBackgroundNotificationsController.setBackgroundNotificationsEnabled(false)
                return@launch
            }

            if (privateMessagesBackgroundNotificationsController.areSystemNotificationsEnabled()) {
                shouldRequestNotificationPermission.value = false
                privateMessagesBackgroundNotificationsController.onNotificationPermissionGranted()
            } else {
                shouldRequestNotificationPermission.value = true
            }
        }
    }

    override fun onAutoplayGifsChanged(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setAutoplayGifs(enabled)
        }
    }

    override fun onThemeOverrideChanged(value: ThemeOverride) {
        viewModelScope.launch {
            appSettings.setThemeOverride(value)
        }
    }

    override fun onDynamicColorsChanged(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setDynamicColorsEnabled(enabled)
        }
    }

    override fun onNotificationPermissionResult(granted: Boolean) {
        shouldRequestNotificationPermission.value = false
        if (!granted) {
            snackbarManager.tryEmit(
                SnackbarEvent(
                    message = SnackbarMessage.Resource(
                        Res.string.settings_snackbar_notifications_permission_required,
                    ),
                ),
            )
            return
        }

        viewModelScope.launch {
            privateMessagesBackgroundNotificationsController.onNotificationPermissionGranted()
        }
    }

    override fun onAnalyticsCollectionChanged(enabled: Boolean) {
        viewModelScope.launch {
            telemetrySettingsController.setAnalyticsEnabled(enabled)
        }
    }

    override fun onCrashReportingChanged(enabled: Boolean) {
        viewModelScope.launch {
            telemetrySettingsController.setCrashReportingEnabled(enabled)
        }
    }

    override fun onClearCacheClicked() {
        viewModelScope.launch {
            val wasCleared = appMaintenanceController.clearCache()
            if (!wasCleared) {
                snackbarManager.tryEmitGenericError()
                return@launch
            }

            snackbarManager.emit(
                SnackbarEvent(
                    message = SnackbarMessage.Resource(Res.string.settings_snackbar_cache_cleared),
                ),
            )
        }
    }

    override fun onCopyDiagnosticsClicked() {
        viewModelScope.launch {
            textClipboardController.setText(buildDiagnosticsSnapshot(state.value))
            snackbarManager.emit(
                SnackbarEvent(
                    message = SnackbarMessage.Resource(Res.string.settings_snackbar_diagnostics_copied),
                ),
            )
        }
    }

    override fun onDebugToolsClicked() {
        appNavigator.navigateTo(DebugScreen)
    }

    override fun onManageBlacklistsClicked() {
        appNavigator.navigateTo(BlacklistsScreen)
    }

    override fun onLogoutClicked() {
        viewModelScope.launch {
            val dialog = GenericDialog.fromResources(
                title = Res.string.dialog_title_logout,
                positiveText = Res.string.dialog_button_confirm,
                negativeText = Res.string.dialog_button_dismiss,
            )
            val confirmed = appNavigator.awaitResult<Boolean>(dialog, dialog.key)
            if (!confirmed) return@launch

            authRepository.logout()
                .onSuccess {
                    isLoggedIn.value = false
                    authSessionEvents.tryEmit(AuthSessionEvent.TokensUpdated)
                }
                .onFailure {
                    logger.error("Failed to logout", it)
                    val stillLoggedIn = authRepository.isLoggedIn()
                    isLoggedIn.value = stillLoggedIn
                    if (!stillLoggedIn) {
                        authSessionEvents.tryEmit(AuthSessionEvent.TokensUpdated)
                    } else {
                        snackbarManager.tryEmitGenericError()
                    }
                }
        }
    }

    private fun refreshLoggedInState() {
        viewModelScope.launch {
            isLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    private fun observeAuthSessionChanges() {
        viewModelScope.launch {
            authSessionEvents.events.collect { event ->
                when (event) {
                    AuthSessionEvent.TokensUpdated -> refreshLoggedInState()
                }
            }
        }
    }

    private fun buildDiagnosticsSnapshot(state: SettingsScreenState): String = buildString {
        appendLine("app=Podkop")
        appendLine("platform=${buildInfo.platformName}")
        appendLine("version=${buildInfo.appVersionName}")
        appendLine("buildType=${if (buildInfo.isDebugBuild) "debug" else "release"}")
        appendLine("loggedIn=${isLoggedIn.value}")
        appendLine("themeOverride=${state.themeOverride.name}")
        appendLine("dynamicColors=${state.dynamicColorsEnabled}")
        appendLine("autoplayGifs=${state.autoplayGifs}")

        if (state.supportsPrivateMessagesBackgroundNotifications) {
            appendLine("pmBackgroundNotifications=${state.privateMessagesBackgroundNotificationsEnabled}")
            appendLine("systemNotificationsEnabled=${state.areSystemNotificationsEnabled}")
        }

        if (state.supportsTelemetryControls) {
            appendLine("analyticsEnabled=${state.analyticsEnabled}")
            appendLine("crashReportingEnabled=${state.crashReportingEnabled}")
        }
    }.trim()
}

private data class SettingsPreferencesState(
    val autoplayGifs: Boolean,
    val themeOverride: ThemeOverride,
    val dynamicColorsEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val crashReportingEnabled: Boolean,
)

private data class VisualPreferencesState(
    val autoplayGifs: Boolean,
    val themeOverride: ThemeOverride,
    val dynamicColorsEnabled: Boolean,
)

private data class TelemetryPreferencesState(val analyticsEnabled: Boolean, val crashReportingEnabled: Boolean)
