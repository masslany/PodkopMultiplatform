package pl.masslany.podkop.features.notifications.models

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup

data class NotificationGroupChipState(val group: NotificationGroup, val unreadCount: Int, val selected: Boolean)
