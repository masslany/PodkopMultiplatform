package pl.masslany.podkop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState

class MainActivityViewModel(
    private val startupManager: StartupManager,
) : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState.Initializing)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            startupManager.state.collect { appState ->
                _state.update { appState }
            }
        }
    }
}
