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

    fun unreadCount(group: NotificationGroup): Int = when (group) {
        NotificationGroup.Entries -> entriesUnreadCount
        NotificationGroup.PrivateMessages -> privateMessagesUnreadCount
        NotificationGroup.Tags -> tagsUnreadCount
        NotificationGroup.ObservedDiscussions -> observedDiscussionsUnreadCount
    }

    fun withUnreadCount(
        group: NotificationGroup,
        unreadCount: Int,
    ): NotificationsStatus = when (group) {
        NotificationGroup.Entries -> copy(entriesUnreadCount = unreadCount)
        NotificationGroup.PrivateMessages -> copy(privateMessagesUnreadCount = unreadCount)
        NotificationGroup.Tags -> copy(tagsUnreadCount = unreadCount)
        NotificationGroup.ObservedDiscussions -> copy(observedDiscussionsUnreadCount = unreadCount)
    }

    fun decrementUnreadCount(group: NotificationGroup): NotificationsStatus =
        withUnreadCount(
            group = group,
            unreadCount = (unreadCount(group) - 1).coerceAtLeast(0),
        )

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
