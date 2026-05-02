package pl.masslany.podkop.test.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState

class FakeStartupManager : StartupManager {
    private val _state = MutableStateFlow<AppState>(AppState.Ready)
    override val state: StateFlow<AppState> = _state.asStateFlow()

    override suspend fun init(
        key: String,
        secret: String,
    ) = Unit

    override suspend fun retry() {
        _state.value = AppState.Ready
    }
}
