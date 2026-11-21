package pl.masslany.podkop.features.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.notifications.domain.models.NotificationActor
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget

class NotificationNavigationExtensionsTest {

    @Test
    fun `private message notifications open actor conversation`() {
        val result = notificationItem(
            group = NotificationGroup.PrivateMessages,
            actor = NotificationActor(
                username = "alice",
                avatarUrl = null,
                gender = Gender.Female,
                nameColor = NameColor.Green,
            ),
        ).navigationTarget()

        assertEquals(NotificationNavigationTarget.Conversation(username = "alice"), result)
    }

    @Test
    fun `private message notifications fall back to id when actor is missing`() {
        val result = notificationItem(
            id = "pm-thread-id",
            group = NotificationGroup.PrivateMessages,
            actor = null,
        ).navigationTarget()

        assertEquals(NotificationNavigationTarget.Conversation(username = "pm-thread-id"), result)
    }

    @Test
    fun `non private message notifications keep existing routing precedence`() {
        val result = notificationItem(
            group = NotificationGroup.Entries,
            linkId = 123,
            entryId = 456,
            profileUsername = "alice",
        ).navigationTarget()

        assertEquals(NotificationNavigationTarget.Link(id = 123), result)
    }
}

private fun notificationItem(
    id: String = "notification-id",
    group: NotificationGroup,
    actor: NotificationActor? = null,
    linkId: Int? = null,
    entryId: Int? = null,
    profileUsername: String? = null,
): NotificationItem = NotificationItem(
    id = id,
    group = group,
    type = null,
    isRead = false,
    groupId = null,
    groupCount = 1,
    showAsGroup = false,
    createdAt = LocalDateTime(2026, 1, 1, 10, 0),
    actor = actor,
    message = null,
    url = null,
    tagName = null,
    profileUsername = profileUsername,
    entryId = entryId,
    entryContent = null,
    linkId = linkId,
    linkTitle = null,
    linkDescription = null,
    badgeName = null,
    issueTitle = null,
)
