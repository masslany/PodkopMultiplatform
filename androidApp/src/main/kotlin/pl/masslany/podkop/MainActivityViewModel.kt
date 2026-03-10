package pl.masslany.podkop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.navigation.AppNavigator

class MainActivityViewModel(
    startupManager: StartupManager,
    navigator: AppNavigator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    init {
        navigator.initialize(savedStateHandle[NAVIGATOR_BACKSTACK_STATE_KEY])

        viewModelScope.launch {
            navigator.state
                .map { navigator.serializeBackStack() }
                .distinctUntilChanged()
                .collect { serializedBackStack ->
                    if (navigator.isReady.value) {
                        serializedBackStack?.let {
                            savedStateHandle[NAVIGATOR_BACKSTACK_STATE_KEY] = it
                        }
                    }
                }
        }
    }

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

    private companion object {
        const val NAVIGATOR_BACKSTACK_STATE_KEY = "main_activity.navigator_back_stack"
    }
}
