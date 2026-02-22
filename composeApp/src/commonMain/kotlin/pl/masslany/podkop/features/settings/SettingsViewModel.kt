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
import pl.masslany.podkop.common.platform.isDebugBuild
import pl.masslany.podkop.common.platform.supportsDynamicColorsToggle
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.tryEmitGenericError
import pl.masslany.podkop.features.debug.DebugScreen
import pl.masslany.podkop.features.topbar.TopBarActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_confirm
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.dialog_title_logout

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val appSettings: AppSettings,
    private val appNavigator: AppNavigator,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager,
    topBarActions: TopBarActions,
) : ViewModel(),
    SettingsActions,
    TopBarActions by topBarActions {
    private val isLoggedIn = MutableStateFlow(false)

    val state = combine(
        appSettings.autoplayGifs,
        appSettings.themeOverride,
        appSettings.dynamicColorsEnabled,
        isLoggedIn,
    ) { autoplayGifs, themeOverride, dynamicColorsEnabled, isLoggedIn ->
        SettingsScreenState(
            autoplayGifs = autoplayGifs,
            themeOverride = themeOverride,
            dynamicColorsEnabled = dynamicColorsEnabled,
            supportsDynamicColorsToggle = supportsDynamicColorsToggle(),
            showDebugTools = isDebugBuild(),
            showLogoutButton = isLoggedIn,
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

    override fun onDebugToolsClicked() {
        appNavigator.navigateTo(DebugScreen)
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
}
