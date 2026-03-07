package pl.masslany.podkop.business.notifications.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class NotificationsPage(
    override val data: List<NotificationItem>,
    override val pagination: Pagination?,
) : PaginatedData<NotificationItem> {
    companion object {
        val empty = NotificationsPage(
            data = emptyList(),
            pagination = null,
        )
    }
}
