package pl.masslany.podkop.features.notifications

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup

object NotificationsTestTags {
    private const val Feature = "notifications"

    object Screen {
        const val List = "$Feature:screen:list"
    }

    object Group {
        fun chip(group: NotificationGroup): String = "$Feature:group:${group.name}"
    }

    object Item {
        fun card(id: String): String = "$Feature:item:card:$id"
    }

    object Grouped {
        fun expand(rowId: String): String = "$Feature:grouped:expand:$rowId"

        fun showMore(rowId: String): String = "$Feature:grouped:show-more:$rowId"
    }
}
