package pl.masslany.podkop.features.notifications

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget

internal fun NotificationItem.navigationTarget(): NotificationNavigationTarget {
    if (group == NotificationGroup.PrivateMessages) {
        return NotificationNavigationTarget.None
    }

    val notificationLinkId = linkId
    if (notificationLinkId != null) {
        return NotificationNavigationTarget.Link(notificationLinkId)
    }

    val notificationEntryId = entryId
    if (notificationEntryId != null) {
        return NotificationNavigationTarget.Entry(notificationEntryId)
    }

    if (!profileUsername.isNullOrBlank()) {
        return NotificationNavigationTarget.Profile(profileUsername.orEmpty())
    }

    if (!tagName.isNullOrBlank()) {
        return NotificationNavigationTarget.Tag(tagName.orEmpty())
    }

    if (!url.isNullOrBlank()) {
        return NotificationNavigationTarget.External(url.orEmpty())
    }

    return NotificationNavigationTarget.None
}
