package pl.masslany.podkop.features.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.features.notifications.models.ObservedNotificationResourceType

class NotificationsScreenStateExtensionsTest {

    @Test
    fun `tag notifications read as content using the tag, not as comments`() {
        val (entry, link) = listOf(
            notificationItem(id = "e", group = NotificationGroup.Tags, entryId = 1),
            notificationItem(id = "l", group = NotificationGroup.Tags, linkId = 2),
        ).toGroupMemberStates()

        assertEquals(ObservedNotificationResourceType.Entry, entry.tagResourceType)
        assertEquals(ObservedNotificationResourceType.Link, link.tagResourceType)
        assertNull(entry.observedResourceType)
        assertEquals("treść e", entry.observedResourceTitle)
    }

    @Test
    fun `observed discussion notifications stay comments`() {
        val state = listOf(notificationItem(id = "c", group = NotificationGroup.ObservedDiscussions, entryId = 1))
            .toNotificationItemStates(NotificationGroup.ObservedDiscussions)
            .single()

        assertEquals(ObservedNotificationResourceType.Entry, state.observedResourceType)
        assertNull(state.tagResourceType)
    }
}

private fun notificationItem(
    id: String,
    group: NotificationGroup,
    linkId: Int? = null,
    entryId: Int? = null,
): NotificationItem = NotificationItem(
    id = id,
    group = group,
    type = null,
    isRead = false,
    groupId = null,
    groupCount = 1,
    showAsGroup = false,
    createdAt = LocalDateTime(2026, 1, 1, 10, 0),
    actor = null,
    message = null,
    url = null,
    tagName = "nauka",
    profileUsername = null,
    entryId = entryId,
    entryContent = "treść $id",
    linkId = linkId,
    linkTitle = "tytuł $id",
    linkDescription = null,
    badgeName = null,
    issueTitle = null,
)
