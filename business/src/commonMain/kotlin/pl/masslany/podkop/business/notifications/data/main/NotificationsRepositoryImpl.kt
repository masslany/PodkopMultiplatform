package pl.masslany.podkop.business.notifications.data.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import kotlin.time.Duration.Companion.minutes

class NotificationsRepositoryImpl(
    private val notificationsDataSource: NotificationsDataSource,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val appScope: CoroutineScope,
) : NotificationsRepository {
    private val mutableUnreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = mutableUnreadCount.asStateFlow()

    private val refreshMutex = Mutex()
    private var pollingJob: Job? = null

    override suspend fun refreshStatus(): Result<NotificationsStatus> {
        return refreshMutex.withLock {
            withContext(dispatcherProvider.io) {
                if (!authRepository.isLoggedIn()) {
                    mutableUnreadCount.value = 0
                    return@withContext Result.success(NotificationsStatus.empty)
                }

                notificationsDataSource.getNotificationsStatus()
                    .mapCatching { it.toNotificationsStatus() }
                    .onSuccess { status ->
                        mutableUnreadCount.value = status.totalUnreadCount
                    }
            }
        }
    }

    override fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = appScope.launch {
            refreshStatus()

            while (isActive) {
                delay(POLLING_INTERVAL)
                refreshStatus()
            }
        }
    }

    override fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun clearUnreadCount() {
        mutableUnreadCount.value = 0
    }

    private companion object {
        val POLLING_INTERVAL = 2.minutes
    }
}
