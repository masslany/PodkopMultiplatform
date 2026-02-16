package pl.masslany.podkop.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.isDebugBuild
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.features.debug.DebugScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class SettingsViewModel(
    private val appSettings: AppSettings,
    private val appNavigator: AppNavigator,
    topBarActions: TopBarActions,
) : ViewModel(),
    SettingsActions,
    TopBarActions by topBarActions {

    val state = appSettings.autoplayGifs
        .map { autoplayGifs ->
            SettingsScreenState(
                autoplayGifs = autoplayGifs,
                showDebugTools = isDebugBuild(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = SettingsScreenState.initial,
        )

    override fun onAutoplayGifsChanged(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setAutoplayGifs(enabled)
        }
    }

    override fun onDebugToolsClicked() {
        appNavigator.navigateTo(DebugScreen)
    }
}
