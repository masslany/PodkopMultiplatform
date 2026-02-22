package pl.masslany.podkop.common.deeplink

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen

class AppDeepLinkHandler internal constructor(
    private val scope: CoroutineScope,
    private val parser: AppDeepLinkParser,
    private val startupManager: StartupManager,
    private val appNavigator: AppNavigator,
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val logger: AppLogger,
) {
    private val incomingUrls = MutableSharedFlow<String>(
        extraBufferCapacity = DEEPLINK_BUFFER_CAPACITY,
    )

    init {
        scope.launch {
            incomingUrls.collect(::handleUrlInternal)
        }
    }

    fun onIncomingUrl(rawUrl: String?) {
        val url = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return
        if (!incomingUrls.tryEmit(url)) {
            logger.warn("Dropping deep link because the queue is full")
        }
    }

    private suspend fun handleUrlInternal(rawUrl: String) {
        when (val deepLink = parser.parse(rawUrl)) {
            null -> {
                logger.debug("Ignoring unsupported deep link")
            }

            is AppDeepLink.LoginCallback -> {
                handleLoginCallback(deepLink)
            }

            is AppDeepLink.LinkDetails -> {
                navigateWhenReady(LinkDetailsScreen(id = deepLink.id))
            }

            is AppDeepLink.EntryDetails -> {
                navigateWhenReady(EntryDetailsScreen(id = deepLink.id))
            }
        }
    }

    private suspend fun handleLoginCallback(deepLink: AppDeepLink.LoginCallback) {
        try {
            authRepository.storeSessionTokens(
                token = deepLink.token,
                refreshToken = deepLink.refreshToken,
            )
        } catch (throwable: Throwable) {
            logger.error("Failed to persist auth tokens from deep link callback", throwable)
            return
        }

        authSessionEvents.tryEmit(AuthSessionEvent.TokensUpdated)
    }

    private suspend fun navigateWhenReady(target: NavTarget) {
        if (!awaitNavigationAvailability()) return
        appNavigator.navigateTo(target)
    }

    private suspend fun awaitNavigationAvailability(): Boolean {
        appNavigator.isReady.first { it }

        return when (startupManager.state.first { it !is AppState.Initializing }) {
            is AppState.Ready -> true

            is AppState.Error -> {
                logger.warn("Skipping deep link navigation because app startup is in error state")
                false
            }

            is AppState.Initializing -> false
        }
    }

    private companion object {
        const val DEEPLINK_BUFFER_CAPACITY = 32
    }
}
