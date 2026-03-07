package pl.masslany.podkop.features.notifications.preview

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.notifications.NotificationsActions

object NoOpNotificationsActions : NotificationsActions {
    override fun onGroupSelected(group: NotificationGroup) = Unit

    override fun onNotificationClicked(id: String) = Unit

    override fun onRefresh() = Unit

    override fun onMarkAllAsReadClicked() = Unit

    override fun onTopBarBackClicked() = Unit
}
