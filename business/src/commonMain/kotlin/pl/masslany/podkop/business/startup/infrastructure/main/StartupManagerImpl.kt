package pl.masslany.podkop.business.startup.infrastructure.main

import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.logging.api.AppLogger

internal class StartupManagerImpl(
    private val configStorage: ConfigStorage,
    private val authRepository: AuthRepository,
    private val logger: AppLogger,
): StartupManager {
    override val state = MutableStateFlow<AppState>(AppState.Initializing)

    override suspend fun init(key: String, secret: String) {
        configStorage.storeApiKey(key)
        configStorage.storeApiSecret(secret)
        refreshStartupState(
            key = key,
            secret = secret,
        )
    }

    override suspend fun retry() {
        refreshStartupState(
            key = configStorage.getApiKey(),
            secret = configStorage.getApiSecret(),
        )
    }

    private suspend fun refreshStartupState(
        key: String,
        secret: String,
    ) {
        state.emit(AppState.Initializing)

        if (key.isBlank() || secret.isBlank()) {
            logger.error("Missing API credentials required to initialize app", null)
            state.emit(AppState.Error)
            return
        }

        if (authRepository.shouldUpdateTokens()) {
            authRepository.updateTokens().fold(
                onSuccess = {
                    state.emit(AppState.Ready)
                },
                onFailure = {
                    logger.error("Failed to get auth token", it)
                    state.emit(AppState.Error)
                }
            )
        } else {
            state.emit(AppState.Ready)
        }
    }
}
