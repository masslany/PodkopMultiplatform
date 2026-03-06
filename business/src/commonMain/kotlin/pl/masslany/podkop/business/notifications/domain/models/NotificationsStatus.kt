package pl.masslany.podkop.business.notifications.domain.models

data class NotificationsStatus(
    val privateMessagesEnabled: Boolean,
    val privateMessagesNotificationsEnabled: Boolean,
    val entryNotificationsEnabled: Boolean,
    val tagNotificationsEnabled: Boolean,
    val observedDiscussionsNotificationsEnabled: Boolean,
    val privateMessagesUnreadCount: Int,
    val entriesUnreadCount: Int,
    val tagsUnreadCount: Int,
    val observedDiscussionsUnreadCount: Int,
) {
    val totalUnreadCount: Int =
        privateMessagesUnreadCount +
            entriesUnreadCount +
            tagsUnreadCount +
            observedDiscussionsUnreadCount

    companion object {
        val empty = NotificationsStatus(
            privateMessagesEnabled = false,
            privateMessagesNotificationsEnabled = false,
            entryNotificationsEnabled = false,
            tagNotificationsEnabled = false,
            observedDiscussionsNotificationsEnabled = false,
            privateMessagesUnreadCount = 0,
            entriesUnreadCount = 0,
            tagsUnreadCount = 0,
            observedDiscussionsUnreadCount = 0,
        )
    }
}
