package pl.masslany.podkop.test.notifications

import pl.masslany.podkop.test.fixtures.NotificationFixtures
import pl.masslany.podkop.test.support.MockApiServer

object NotificationsRoutes {
    const val NOTIFICATIONS_PATH = "/api/v3/notifications"
    const val GROUPED_SAMPLE = "notifications-tags-grouped-user"
    const val MEMBERS_SAMPLE = "notifications-group-members-user"

    /** The first row: an unread group of new entries in an observed tag, as in the sample. */
    val FIRST_GROUP_ID = NotificationFixtures.groupId(1)

    private val firstGroupedPage = mapOf("page" to "1", "show_grouped" to "1")

    /**
     * Tag notifications as a signed-in user got them on 2026-10-09, asked for the way website does:
     * one row per group (`show_grouped=1`), and a group's own notifications in numbered pages.
     */
    fun MockApiServer.groupedTagNotifications() {
        // The screen opens on entry notifications, of which this user had none.
        getJson(
            path = "$NOTIFICATIONS_PATH/entries",
            query = firstGroupedPage,
            body = sample("notifications-entries-empty-user").toString(),
        )
        getJson(
            path = "$NOTIFICATIONS_PATH/tags",
            query = firstGroupedPage,
            body = NotificationFixtures.groupedPage(sample(GROUPED_SAMPLE)),
        )
        val members = sample(MEMBERS_SAMPLE)
        (1..3).forEach { page ->
            getJson(
                path = "$NOTIFICATIONS_PATH/groups/$FIRST_GROUP_ID",
                query = mapOf("page" to "$page"),
                body = NotificationFixtures.membersPage(members, FIRST_GROUP_ID, page, read = false),
            )
        }
    }

    fun MockApiServer.markTagNotificationRead(id: String) {
        put(path = "$NOTIFICATIONS_PATH/tags/$id")
    }
}
