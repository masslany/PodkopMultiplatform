package pl.masslany.podkop.business.privatemessages.data.main

import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakePrivateMessagesDataSource
import pl.masslany.podkop.business.common.data.network.models.common.EmbedDto
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageConversationDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageMediaDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDataDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageUserDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

class PrivateMessagesRepositoryImplTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `get conversations maps conversation list`() = runBlocking {
        val dataSource = FakePrivateMessagesDataSource().apply {
            getConversationsResult = Result.success(
                PrivateMessagesListDto(
                    data = listOf(
                        PrivateMessageConversationDto(
                            user = PrivateMessageUserDto(
                                username = "sender",
                                avatar = "",
                                gender = null,
                                color = "orange",
                            ),
                            lastMessage = PrivateMessageDto(
                                content = "hello",
                                createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                                key = "msg-1",
                            ),
                            unread = true,
                        ),
                    ),
                    pagination = null,
                ),
            )
        }
        val sut = PrivateMessagesRepositoryImpl(
            privateMessagesDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getConversations(page = 2).getOrThrow()

        assertEquals(listOf(2), dataSource.getConversationsCalls)
        assertEquals("sender", actual.data.single().username)
        assertEquals("hello", actual.data.single().lastMessageContent)
        assertTrue(actual.data.single().unread)
    }

    @Test
    fun `get conversation messages maps message thread`() = runBlocking {
        val dataSource = FakePrivateMessagesDataSource().apply {
            getConversationMessagesResult = Result.success(
                PrivateMessageThreadDto(
                    data = PrivateMessageThreadDataDto(
                        user = PrivateMessageUserDto(
                            username = "sender",
                            avatar = "",
                            gender = "m",
                            color = "orange",
                        ),
                        messages = listOf(
                            PrivateMessageDto(
                                content = "reply",
                                adult = false,
                                type = 1,
                                createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                                media = PrivateMessageMediaDto(
                                    photo = null,
                                    embed = EmbedDto(
                                        key = "embed-key",
                                        thumbnail = "https://example.com/thumb.jpg",
                                        type = "youtube",
                                        url = "https://example.com",
                                    ),
                                ),
                                read = false,
                                key = "msg-2",
                                user = PrivateMessageUserDto(
                                    username = "sender",
                                    avatar = "https://example.com/avatar.jpg",
                                    gender = "m",
                                    color = "orange",
                                ),
                            ),
                        ),
                    ),
                    pagination = null,
                ),
            )
        }
        val sut = PrivateMessagesRepositoryImpl(
            privateMessagesDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getConversationMessages(username = "sender", page = 3).getOrThrow()

        assertEquals(
            listOf(FakePrivateMessagesDataSource.GetConversationMessagesCall("sender", 3)),
            dataSource.getConversationMessagesCalls,
        )
        assertEquals("msg-2", actual.data.single().key)
        assertEquals("reply", actual.data.single().content)
        assertFalse(actual.data.single().isRead)
        assertEquals("sender", actual.data.single().sender?.username)
        assertNull(actual.data.single().photo)
        assertEquals("https://example.com", actual.data.single().embed?.url)
    }

    @Test
    fun `get conversation messages accepts thread payload with top level user and message list`() = runBlocking {
        val dataSource = FakePrivateMessagesDataSource().apply {
            getConversationMessagesResult = Result.success(
                PrivateMessageThreadDto(
                    data = PrivateMessageThreadDataDto(
                        user = PrivateMessageUserDto(
                            username = "sender",
                            avatar = "",
                            gender = null,
                            color = "orange",
                        ),
                        messages = listOf(
                            PrivateMessageDto(
                                content = "reply",
                                adult = false,
                                type = 1,
                                createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                                media = null,
                                read = true,
                                key = "msg-4",
                                user = null,
                            ),
                        ),
                    ),
                    pagination = null,
                ),
            )
        }
        val sut = PrivateMessagesRepositoryImpl(
            privateMessagesDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getConversationMessages(username = "sender", page = 1).getOrThrow()

        assertEquals("msg-4", actual.data.single().key)
        assertEquals("reply", actual.data.single().content)
        assertNull(actual.data.single().sender)
    }

    @Test
    fun `thread dto treats false data as empty thread`() {
        val actual = json.decodeFromString<PrivateMessageThreadDto>(
            """
            {"data":false}
            """.trimIndent(),
        )

        assertEquals(emptyList(), actual.data.messages)
        assertNull(actual.data.user)
    }

    @Test
    fun `open conversation forwards request data and maps created message`() = runBlocking {
        val dataSource = FakePrivateMessagesDataSource().apply {
            openConversationResult = Result.success(
                PrivateMessageItemResponseDto(
                    data = PrivateMessageDto(
                        content = "hello",
                        adult = true,
                        type = 1,
                        createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                        media = PrivateMessageMediaDto(
                            photo = PhotoDto(
                                height = 600,
                                key = "photo-key",
                                label = "screenshot",
                                mimeType = "image/gif",
                                size = 11858,
                                url = "https://example.com/photo.jpg",
                                width = 800,
                            ),
                            embed = EmbedDto(
                                key = "embed-key",
                                thumbnail = "https://example.com/thumb.jpg",
                                type = "youtube",
                                url = "https://example.com",
                            ),
                        ),
                        read = true,
                        key = "msg-3",
                        user = null,
                    ),
                ),
            )
            readAllResult = Result.success(Unit)
        }
        val sut = PrivateMessagesRepositoryImpl(
            privateMessagesDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val message = sut.openConversation(
            username = "receiver",
            content = "hello",
            adult = true,
            photoKey = "photo-key",
            embed = "https://example.com",
        ).getOrThrow()
        val readAll = sut.readAll()

        assertEquals(
            listOf(
                FakePrivateMessagesDataSource.OpenConversationCall(
                    username = "receiver",
                    content = "hello",
                    adult = true,
                    photoKey = "photo-key",
                    embed = "https://example.com",
                ),
            ),
            dataSource.openConversationCalls,
        )
        assertTrue(readAll.isSuccess)
        assertEquals(1, dataSource.readAllCalls)
        assertEquals("msg-3", message.key)
        assertTrue(message.isRead)
        assertEquals("https://example.com/photo.jpg", message.photo?.url)
        assertEquals(800, message.photo?.width)
        assertEquals(600, message.photo?.height)
        assertEquals("https://example.com", message.embed?.url)
    }
}
