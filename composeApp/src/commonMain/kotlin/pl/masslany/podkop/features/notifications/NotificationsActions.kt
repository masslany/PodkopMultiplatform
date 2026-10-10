package pl.masslany.podkop.features.notifications

import androidx.compose.runtime.Stable
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface NotificationsActions : TopBarActions {
    fun onGroupSelected(group: NotificationGroup)

    fun onNotificationClicked(id: String)

    /** Shows or hides the notifications inside a grouped row, like website's "Rozwiń". */
    fun onGroupedRowExpandToggled(id: String)

    /** Loads the next page of an expanded group; website only ever shows the first one. */
    fun onGroupedRowShowMoreClicked(id: String)

    fun onGroupedRowNotificationClicked(
        rowId: String,
        id: String,
    )

    fun onRefresh()

    fun onMarkAllAsReadClicked()

    /** Loads the next page; after a failed one, the list's retry button asks for it again. */
    fun paginate()
}
