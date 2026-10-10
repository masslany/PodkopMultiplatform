package pl.masslany.podkop.features.notifications.preview

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.notifications.NotificationsActions

object NoOpNotificationsActions : NotificationsActions {
    override fun onGroupSelected(group: NotificationGroup) = Unit
    override fun onNotificationClicked(id: String) = Unit
    override fun onGroupedRowExpandToggled(id: String) = Unit
    override fun onGroupedRowShowMoreClicked(id: String) = Unit
    override fun onGroupedRowNotificationClicked(rowId: String, id: String) = Unit
    override fun onRefresh() = Unit
    override fun onMarkAllAsReadClicked() = Unit
    override fun onTopBarBackClicked() = Unit
    override fun onTopBarSearchClicked() = Unit
    override fun onTopBarNotificationsClicked() = Unit
    override fun onTopBarAddEntryClicked() = Unit
    override fun onTopBarAddLinkClicked() = Unit
}
