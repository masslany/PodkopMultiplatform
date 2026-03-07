package pl.masslany.podkop.business.notifications.data.main

import pl.masslany.podkop.business.notifications.domain.models.NotificationActor
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationsPage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage

internal fun PrivateMessagesPage.toNotificationsPage(): NotificationsPage =
    NotificationsPage(
        data = data.map(PrivateMessageConversation::toNotificationItem),
        pagination = pagination,
    )

private fun PrivateMessageConversation.toNotificationItem(): NotificationItem = NotificationItem(
    id = username,
    group = NotificationGroup.PrivateMessages,
    type = "private_message",
    isRead = !unread,
    groupId = null,
    groupCount = 1,
    showAsGroup = false,
    createdAt = lastMessageCreatedAt,
    actor = NotificationActor(
        username = username,
        avatarUrl = avatarUrl,
        gender = gender,
        nameColor = nameColor,
    ),
    message = lastMessageContent,
    url = null,
    tagName = null,
    profileUsername = null,
    entryId = null,
    entryContent = null,
    linkId = null,
    linkTitle = null,
    linkDescription = null,
    badgeName = null,
    issueTitle = null,
)
