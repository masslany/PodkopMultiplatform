package pl.masslany.podkop.business.startup.api

import kotlinx.coroutines.flow.StateFlow
import pl.masslany.podkop.business.startup.models.AppState

interface StartupManager {
    val state: StateFlow<AppState>

    suspend fun init(
        key: String,
        secret: String,
    )
}
