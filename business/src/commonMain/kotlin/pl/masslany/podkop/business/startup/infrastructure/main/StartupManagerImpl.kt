package pl.masslany.podkop.business.startup.infrastructure.main

import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.configstorage.api.ConfigStorage

internal class StartupManagerImpl(
    private val configStorage: ConfigStorage,
    private val authRepository: AuthRepository,
): StartupManager {
    override val state = MutableStateFlow<AppState>(AppState.Initializing)

    override suspend fun init(key: String, secret: String) {
        configStorage.storeApiKey(key)
        configStorage.storeApiSecret(secret)
        if (authRepository.shouldUpdateTokens()) {
            authRepository.updateTokens().fold(
                onSuccess = {
                    state.emit(AppState.Ready)
                },
                onFailure = {
                    println("Failed to get auth token $it")
                    state.emit(AppState.Error)
                }
            )
        } else {
            state.emit(AppState.Ready)
        }
    }
}
