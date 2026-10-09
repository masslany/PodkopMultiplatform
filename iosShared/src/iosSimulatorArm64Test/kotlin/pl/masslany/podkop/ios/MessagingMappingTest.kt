package pl.masslany.podkop.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.notifications.domain.models.NotificationActor
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.pagination.nextRequest
import pl.masslany.podkop.features.pagination.FeaturePaginationPolicies

class MessagingMappingTest {
    private fun notification(group: NotificationGroup, linkId: Int? = null, url: String? = null) = NotificationItem(
        id = "n1", group = group, type = null, isRead = false, groupId = "g", groupCount = 2, showAsGroup = true,
        createdAt = LocalDateTime(2026, 3, 1, 12, 0), actor = NotificationActor("ewa", null, Gender.Female, NameColor.Green),
        message = "m", url = url, tagName = null, profileUsername = null, entryId = null, entryContent = null,
        linkId = linkId, linkTitle = null, linkDescription = null, badgeName = null, issueTitle = null,
    )

    @Test
    fun notificationsCarryTheirSharedTargetAndGroupName() {
        val link = notification(NotificationGroup.ObservedDiscussions, linkId = 7).toIOS()
        assertEquals("observedDiscussions", link.group)
        assertEquals("link", link.targetKind)
        assertEquals("7", link.targetValue)
        assertEquals("green", link.actorColor)
        val pm = notification(NotificationGroup.PrivateMessages, linkId = 7).toIOS()
        assertEquals("conversation", pm.targetKind)
        assertEquals("ewa", pm.targetValue)
        assertEquals("none", notification(NotificationGroup.Entries).toIOS().targetKind)
    }

    @Test
    fun notificationGroupsUseAndroidPagingPolicies() {
        val pm = FeaturePaginationPolicies.notifications("pm".toNotificationGroup())
        assertEquals(PageRequest.Number(1), pm.initialRequest())
        val tags = FeaturePaginationPolicies.notifications("tags".toNotificationGroup())
        assertEquals(PageRequest.Number(1), tags.initialRequest())
        assertEquals(PageRequest.Number(2), tags.nextRequest("", 2))
    }

    @Test
    fun messagesMapDirectionAndMonotonicTime() {
        val earlier = PrivateMessage("a", "x", LocalDateTime(2026, 3, 1, 12, 0), true, false, 1, null, null, null).toIOS()
        val later = PrivateMessage("b", null, LocalDateTime(2026, 3, 1, 12, 0, 30), true, true, 2, null, null, null).toIOS()
        assertTrue(earlier.incoming)
        assertTrue(!later.incoming)
        assertTrue(later.createdAtEpochMillis - earlier.createdAtEpochMillis == 30_000L)
    }
}
