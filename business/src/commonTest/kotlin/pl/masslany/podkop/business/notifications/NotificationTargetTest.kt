package pl.masslany.podkop.business.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationTarget
import pl.masslany.podkop.business.notifications.domain.models.target

class NotificationTargetTest {
    private fun item(
        group: NotificationGroup = NotificationGroup.Entries,
        linkId: Int? = null,
        entryId: Int? = null,
        profile: String? = null,
        tag: String? = null,
        url: String? = null,
    ) = NotificationItem(
        id = "n1", group = group, type = null, isRead = false, groupId = null, groupCount = 1,
        showAsGroup = false, createdAt = LocalDateTime(2026, 1, 1, 10, 0), actor = null, message = null,
        url = url, tagName = tag, profileUsername = profile, entryId = entryId, entryContent = null,
        linkId = linkId, linkTitle = null, linkDescription = null, badgeName = null, issueTitle = null,
    )

    @Test
    fun `targets are resolved in Android priority order`() {
        assertEquals(NotificationTarget.Link(1), item(linkId = 1, entryId = 2, tag = "t").target())
        assertEquals(NotificationTarget.Entry(2), item(entryId = 2, profile = "p").target())
        assertEquals(NotificationTarget.Profile("p"), item(profile = "p", tag = "t").target())
        assertEquals(NotificationTarget.Tag("t"), item(tag = "t", url = "https://x").target())
        assertEquals(NotificationTarget.External("https://x"), item(url = "https://x").target())
        assertEquals(NotificationTarget.None, item(profile = " ").target())
    }

    @Test
    fun `private message notifications open the conversation, falling back to the id`() {
        assertEquals(
            NotificationTarget.Conversation("n1"),
            item(group = NotificationGroup.PrivateMessages, linkId = 1).target(),
        )
    }
}
