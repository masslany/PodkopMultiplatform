package pl.masslany.podkop.features.privatemessages.inbox

import kotlinx.coroutines.flow.Flow

interface PrivateMessagesBackgroundNotificationsController {
    val supportsSettings: Boolean
    val backgroundNotificationsEnabled: Flow<Boolean>

    fun areSystemNotificationsEnabled(): Boolean

    suspend fun onNotificationPermissionGranted()

    suspend fun setBackgroundNotificationsEnabled(enabled: Boolean)

    suspend fun syncScheduling()

    suspend fun onLoggedOut()

    suspend fun updateObservedUnreadCount(unreadCount: Int)

    suspend fun showDebugPrivateMessagesNotification(): Boolean
}
