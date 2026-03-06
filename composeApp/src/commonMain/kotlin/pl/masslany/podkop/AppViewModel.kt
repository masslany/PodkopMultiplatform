package pl.masslany.podkop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.deeplink.AuthSessionEvent
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.NavigationState

class AppViewModel(
    private val startupManager: StartupManager,
    private val appNavigator: AppNavigator,
    private val authRepository: AuthRepository,
    private val authSessionEvents: AuthSessionEvents,
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {
    val startupState: StateFlow<AppState> = startupManager.state
    val navigationState: StateFlow<NavigationState> = appNavigator.state

    init {
        observeAuthSessionChanges()
    }

    fun onBack() {
        appNavigator.back()
    }

    fun onAppForegrounded() {
        notificationsRepository.startPolling()
    }

    fun onAppBackgrounded() {
        notificationsRepository.stopPolling()
    }

    private fun observeAuthSessionChanges() {
        viewModelScope.launch {
            authSessionEvents.events.collect { event ->
                when (event) {
                    AuthSessionEvent.TokensUpdated -> {
                        if (authRepository.isLoggedIn()) {
                            notificationsRepository.refreshStatus()
                        } else {
                            notificationsRepository.clearUnreadCount()
                        }
                    }
                }
            }
        }
    }
}
