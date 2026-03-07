package pl.masslany.podkop.features.notifications

import androidx.compose.runtime.Stable
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface NotificationsActions : TopBarActions {
    fun onGroupSelected(group: NotificationGroup)

    fun onNotificationClicked(id: String)

    fun onRefresh()

    fun onMarkAllAsReadClicked()
}
