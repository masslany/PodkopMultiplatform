package pl.masslany.podkop.features.privatemessages.inbox

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoOpPrivateMessagesBackgroundNotificationsController : PrivateMessagesBackgroundNotificationsController {
    override val supportsSettings: Boolean = false
    override val backgroundNotificationsEnabled: Flow<Boolean> = flowOf(false)

    override fun areSystemNotificationsEnabled(): Boolean = false

    override suspend fun onNotificationPermissionGranted() = Unit

    override suspend fun setBackgroundNotificationsEnabled(enabled: Boolean) = Unit

    override suspend fun syncScheduling() = Unit

    override suspend fun onLoggedOut() = Unit

    override suspend fun updateObservedUnreadCount(unreadCount: Int) = Unit

    override suspend fun showDebugPrivateMessagesNotification(): Boolean = false
}
