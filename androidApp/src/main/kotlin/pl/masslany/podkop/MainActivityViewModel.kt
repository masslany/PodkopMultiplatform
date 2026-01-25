package pl.masslany.podkop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.navigation.AppNavigator

class MainActivityViewModel(
    startupManager: StartupManager,
    navigator: AppNavigator,
) : ViewModel() {

    val state = combine(
        startupManager.state,
        navigator.isReady,
    ) { startupState, isReady ->
        if (!isReady) {
            AppState.Initializing
        } else {
            startupState
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppState.Initializing
        )

}
