package pl.masslany.podkop.business.privatemessages.data.main

import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakePrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageConversationDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageMediaDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageUserDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

class PrivateMessagesRepositoryImplTest {

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

        assertEquals(listOf<Any?>(2), dataSource.getConversationsCalls)
        assertEquals("sender", actual.data.single().username)
        assertEquals("hello", actual.data.single().lastMessageContent)
        assertTrue(actual.data.single().unread)
    }

    @Test
    fun `get conversation messages maps message thread`() = runBlocking {
        val dataSource = FakePrivateMessagesDataSource().apply {
            getConversationMessagesResult = Result.success(
                PrivateMessageThreadDto(
                    data = listOf(
                        PrivateMessageDto(
                            content = "reply",
                            adult = false,
                            type = 1,
                            createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                            media = PrivateMessageMediaDto(
                                photo = "",
                                embed = "https://example.com",
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
        assertNull(actual.data.single().mediaPhotoUrl)
        assertEquals("https://example.com", actual.data.single().mediaEmbedUrl)
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
                        media = null,
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
    }
}
