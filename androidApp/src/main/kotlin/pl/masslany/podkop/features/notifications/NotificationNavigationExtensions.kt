package pl.masslany.podkop.features.notifications

import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationTarget
import pl.masslany.podkop.business.notifications.domain.models.target
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget

internal fun NotificationItem.navigationTarget(): NotificationNavigationTarget = when (val value = target()) {
    is NotificationTarget.Conversation -> NotificationNavigationTarget.Conversation(value.username)
    is NotificationTarget.Link -> NotificationNavigationTarget.Link(value.id)
    is NotificationTarget.Entry -> NotificationNavigationTarget.Entry(value.id)
    is NotificationTarget.Profile -> NotificationNavigationTarget.Profile(value.username)
    is NotificationTarget.Tag -> NotificationNavigationTarget.Tag(value.name)
    is NotificationTarget.External -> NotificationNavigationTarget.External(value.url)
    NotificationTarget.None -> NotificationNavigationTarget.None
}
