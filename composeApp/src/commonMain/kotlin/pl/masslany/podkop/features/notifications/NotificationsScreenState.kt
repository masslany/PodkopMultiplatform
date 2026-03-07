package pl.masslany.podkop.features.notifications

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.notifications.models.NotificationGroupChipState
import pl.masslany.podkop.features.notifications.models.NotificationListItemState

data class NotificationsScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val isMarkingAllAsRead: Boolean,
    val selectedGroup: NotificationGroup,
    val groups: ImmutableList<NotificationGroupChipState>,
    val items: ImmutableList<NotificationListItemState>,
) {
    val canMarkAllAsRead: Boolean =
        groups.firstOrNull { chip -> chip.group == selectedGroup }?.unreadCount?.let { it > 0 } == true

    companion object {
        val initial = NotificationsScreenState(
            isLoading = true,
            isRefreshing = false,
            isError = false,
            isPaginating = false,
            isMarkingAllAsRead = false,
            selectedGroup = NotificationGroup.Entries,
            groups = persistentListOf(),
            items = persistentListOf(),
        )
    }
}
