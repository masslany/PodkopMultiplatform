package pl.masslany.podkop.features.privatemessages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageConversation
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageSender
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.features.privatemessages.models.mergePrivateConversationMessages
import pl.masslany.podkop.features.privatemessages.models.normalizePrivateMessageUsername
import pl.masslany.podkop.features.privatemessages.models.toConversationMessageItemStates
import pl.masslany.podkop.features.privatemessages.models.toInboxConversationItemStates

class PrivateMessagesModelsTest {

    @Test
    fun `mergePrivateConversationMessages sorts chronologically and replaces duplicates by key`() {
        val merged = mergePrivateConversationMessages(
            existing = listOf(
                privateMessage(
                    key = "2",
                    createdAt = LocalDateTime(2026, 1, 1, 10, 2),
                    content = "outdated",
                ),
                privateMessage(
                    key = "3",
                    createdAt = LocalDateTime(2026, 1, 1, 10, 3),
                    content = "third",
                ),
            ),
            incoming = listOf(
                privateMessage(
                    key = "1",
                    createdAt = LocalDateTime(2026, 1, 1, 10, 1),
                    content = "first",
                ),
                privateMessage(
                    key = "2",
                    createdAt = LocalDateTime(2026, 1, 1, 10, 2),
                    content = "updated",
                ),
            ),
        )

        assertEquals(listOf("1", "2", "3"), merged.map(PrivateMessage::key))
        assertEquals("updated", merged.first { it.key == "2" }.content)
    }

    @Test
    fun `toConversationMessageItemStates uses api type to decide incoming ownership`() {
        val items = listOf(
            privateMessage(
                key = "incoming",
                createdAt = LocalDateTime(2100, 1, 1, 10, 0),
                content = "hello",
                type = 1,
                sender = sender(username = "alice", color = NameColor.Black),
            ),
            privateMessage(
                key = "outgoing-null-sender",
                createdAt = LocalDateTime(2100, 1, 1, 10, 1),
                content = "reply",
                type = 0,
                sender = null,
            ),
            privateMessage(
                key = "incoming-null-sender",
                createdAt = LocalDateTime(2100, 1, 1, 10, 2),
                content = "reply 2",
                type = 1,
                sender = null,
            ),
        ).toConversationMessageItemStates()

        assertTrue(items[0].isIncoming)
        assertFalse(items[1].isIncoming)
        assertTrue(items[2].isIncoming)
        assertEquals("alice", items[0].senderName)
        assertEquals(PublishedTimeType.Now, items[0].publishedAt)
    }

    @Test
    fun `toConversationMessageItemStates maps shared photo model into embed image state`() {
        val item = listOf(
            privateMessage(
                key = "with-photo",
                createdAt = LocalDateTime(2100, 1, 1, 10, 0),
                content = "hello",
                photo = Photo(
                    height = 675,
                    key = "photo-key",
                    label = "29",
                    mimeType = "image/jpeg",
                    size = 91169,
                    url = "https://example.com/photo.jpg",
                    width = 1200,
                ),
            ),
        ).toConversationMessageItemStates().single()

        assertEquals("https://example.com/photo.jpg", item.embedImageState?.url)
        assertEquals("photo-key", item.embedImageState?.key)
        assertEquals("29", item.embedImageState?.source)
        assertEquals(1200, item.embedImageState?.width)
        assertEquals(675, item.embedImageState?.height)
        assertFalse(item.embedImageState?.isGif ?: true)
        assertNull(item.embedUrl)
    }

    @Test
    fun `toInboxConversationItemStates maps null preview to empty string`() {
        val items = listOf(
            PrivateMessageConversation(
                username = "alice",
                avatarUrl = "https://example.com/avatar.jpg",
                gender = Gender.Female,
                nameColor = NameColor.Green,
                lastMessageKey = "pm-1",
                lastMessageContent = null,
                lastMessageCreatedAt = LocalDateTime(2100, 1, 1, 12, 0),
                unread = true,
            ),
        ).toInboxConversationItemStates()

        assertEquals(1, items.size)
        assertEquals("alice", items.single().username)
        assertEquals("", items.single().lastMessagePreview)
        assertEquals(PublishedTimeType.Now, items.single().publishedAt)
        assertTrue(items.single().unread)
    }

    @Test
    fun `normalizePrivateMessageUsername trims whitespace and strips leading at sign`() {
        assertEquals("alice", "  @alice  ".normalizePrivateMessageUsername())
    }
}

private fun privateMessage(
    key: String,
    createdAt: LocalDateTime,
    content: String,
    type: Int? = null,
    sender: PrivateMessageSender? = null,
    photo: Photo? = null,
): PrivateMessage = PrivateMessage(
    key = key,
    content = content,
    createdAt = createdAt,
    isRead = false,
    adult = false,
    type = type,
    sender = sender,
    photo = photo,
    embed = null,
)

private fun sender(
    username: String,
    color: NameColor = NameColor.Orange,
): PrivateMessageSender = PrivateMessageSender(
    username = username,
    avatarUrl = "https://example.com/$username.jpg",
    gender = Gender.Male,
    nameColor = color,
)
