package pl.masslany.podkop.business.notifications.data.main

import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus

internal fun NotificationsStatusDto.toNotificationsStatus(): NotificationsStatus =
    NotificationsStatus(
        privateMessagesEnabled = data.pm,
        privateMessagesNotificationsEnabled = data.pmNotification,
        entryNotificationsEnabled = data.entryNotification,
        tagNotificationsEnabled = data.tagNotification,
        observedDiscussionsNotificationsEnabled = data.observedDiscussionsNotification,
        privateMessagesUnreadCount = data.pmNotificationCount,
        entriesUnreadCount = data.entryNotificationCount,
        tagsUnreadCount = data.tagNotificationCount,
        observedDiscussionsUnreadCount = data.observedDiscussionsNotificationCount,
    )
