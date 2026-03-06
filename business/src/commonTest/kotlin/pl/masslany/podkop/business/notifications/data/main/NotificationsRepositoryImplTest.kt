package pl.masslany.podkop.business.notifications.data.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.testsupport.fakes.FakeAuthDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeNotificationsDataSource
import pl.masslany.podkop.business.auth.data.main.AuthRepositoryImpl
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDataDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsStatusDto

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
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = true
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
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
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = false
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
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
            authRepository = AuthRepositoryImpl(
                authDataSource = FakeAuthDataSource().apply {
                    isLoggedInValue = true
                },
            ),
            dispatcherProvider = FakeDispatcherProvider(),
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )
        sut.refreshStatus()
        notificationsDataSource.getNotificationsStatusResult = Result.failure(expected)

        val actual = sut.refreshStatus()

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
        assertEquals(3, sut.unreadCount.value)
    }
}
