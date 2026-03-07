package pl.masslany.podkop.features.privatemessages.inbox

class NoOpPrivateMessagesBackgroundNotificationsController : PrivateMessagesBackgroundNotificationsController {
    override fun areSystemNotificationsEnabled(): Boolean = false

    override suspend fun onNotificationPermissionGranted() = Unit

    override suspend fun syncScheduling() = Unit

    override suspend fun onLoggedOut() = Unit

    override suspend fun updateObservedUnreadCount(unreadCount: Int) = Unit

    override suspend fun showDebugPrivateMessagesNotification(): Boolean = false
}
