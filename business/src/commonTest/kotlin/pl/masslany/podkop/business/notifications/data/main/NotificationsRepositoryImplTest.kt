package pl.masslany.podkop.business.notifications.data.main

import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.auth.data.main.AuthRepositoryImpl
import pl.masslany.podkop.business.testsupport.fakes.FakeAuthDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeNotificationsDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakePrivateMessagesDataSource
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDataDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageConversationDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageUserDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.pagination.PageRequest

class NotificationsRepositoryImplTest {

    @Test
    fun `refresh status updates unread count with total from dto`() = runBlocking {
        val notificationsDataSource = FakeNotificationsDataSource().apply {
            getNotificationsStatusResult = Result.success(
                NotificationsStatusDto(
                    data = NotificationsStatusDataDto(
                        pm = true,
                        pmNotification = true,
                        entryNotification = true,
                        tagNotification = true,
                        observedDiscussionsNotification = true,
                        pmNotificationCount = 1,
                        entryNotificationCount = 0,
                        tagNotificationCount = 3,
                        observedDiscussionsNotificationCount = 2,
                    ),
                ),
            )
        }
        val sut = NotificationsRepositoryImpl(
            notificationsDataSource = notificationsDataSource,
            privateMessagesDataSource = FakePrivateMessagesDataSource(),
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = true
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        val actual = sut.refreshStatus()

        assertEquals(6, actual.getOrThrow().totalUnreadCount)
        assertEquals(6, sut.unreadCount.value)
        assertEquals(1, notificationsDataSource.getNotificationsStatusCalls)
    }

    @Test
    fun `refresh status clears unread count and skips request when user is logged out`() = runBlocking {
        val notificationsDataSource = FakeNotificationsDataSource()
        val sut = NotificationsRepositoryImpl(
            notificationsDataSource = notificationsDataSource,
            privateMessagesDataSource = FakePrivateMessagesDataSource(),
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = false
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            logger = NoOpLogger,
        )
        sut.clearUnreadCount()

        val actual = sut.refreshStatus()

        assertTrue(actual.isSuccess)
        assertEquals(0, sut.unreadCount.value)
        assertEquals(0, notificationsDataSource.getNotificationsStatusCalls)
    }

    @Test
    fun `refresh status keeps previous unread count when request fails`() = runBlocking {
        val expected = IllegalStateException("boom")
        val notificationsDataSource = FakeNotificationsDataSource().apply {
            getNotificationsStatusResult = Result.success(
                NotificationsStatusDto(
                    data = NotificationsStatusDataDto(
                        pm = true,
                        pmNotification = true,
                        entryNotification = true,
                        tagNotification = true,
                        observedDiscussionsNotification = true,
                        pmNotificationCount = 1,
                        entryNotificationCount = 0,
                        tagNotificationCount = 2,
                        observedDiscussionsNotificationCount = 0,
                    ),
                ),
            )
        }
        val sut = NotificationsRepositoryImpl(
            notificationsDataSource = notificationsDataSource,
            privateMessagesDataSource = FakePrivateMessagesDataSource(),
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = true
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            logger = NoOpLogger,
        )
        sut.refreshStatus()
        notificationsDataSource.getNotificationsStatusResult = Result.failure(expected)

        val actual = sut.refreshStatus()

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
        assertEquals(3, sut.unreadCount.value)
    }

    @Test
    fun `private messages group is loaded from conversations endpoint`() = runBlocking {
        val privateMessagesDataSource = FakePrivateMessagesDataSource().apply {
            getConversationsResult = Result.success(
                PrivateMessagesListDto(
                    data = listOf(
                        PrivateMessageConversationDto(
                            user = PrivateMessageUserDto(
                                username = "ZjemCiWanne",
                                avatar = null,
                                gender = null,
                                color = "orange",
                            ),
                            lastMessage = PrivateMessageDto(
                                content = "2137",
                                createdAt = LocalDateTime.parse("2026-03-07T00:11:32"),
                                key = "gNQ0e6M6",
                            ),
                            unread = true,
                        ),
                    ),
                    pagination = null,
                ),
            )
        }
        val sut = NotificationsRepositoryImpl(
            notificationsDataSource = FakeNotificationsDataSource(),
            privateMessagesDataSource = privateMessagesDataSource,
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = true
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            logger = NoOpLogger,
        )

        val actual = sut.getNotifications(group = NotificationGroup.PrivateMessages, page = PageRequest.Number(1))

        val item = actual.getOrThrow().data.single()
        assertEquals(listOf(1), privateMessagesDataSource.getConversationsCalls)
        assertEquals(NotificationGroup.PrivateMessages, item.group)
        assertEquals("ZjemCiWanne", item.id)
        assertEquals("ZjemCiWanne", item.actor?.username)
        assertEquals("2137", item.message)
        assertEquals(false, item.isRead)
    }
}

private object NoOpLogger : AppLogger {
    override fun debug(message: String) = Unit

    override fun info(message: String) = Unit

    override fun warn(message: String, throwable: Throwable?) = Unit

    override fun error(message: String, throwable: Throwable?) = Unit
}
