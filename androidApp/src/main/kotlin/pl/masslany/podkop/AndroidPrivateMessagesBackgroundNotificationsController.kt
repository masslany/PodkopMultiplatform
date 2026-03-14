package pl.masslany.podkop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesBackgroundNotificationsController
import java.util.concurrent.TimeUnit

class AndroidPrivateMessagesBackgroundNotificationsController(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val notificationsRepository: NotificationsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val logger: AppLogger,
) : PrivateMessagesBackgroundNotificationsController {
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(application)
    }

    override val supportsSettings: Boolean = true

    override val backgroundNotificationsEnabled: Flow<Boolean> =
        keyValueStorage.observeBoolean(BACKGROUND_NOTIFICATIONS_ENABLED_KEY)
            .map { it ?: false }
            .distinctUntilChanged()

    override fun areSystemNotificationsEnabled(): Boolean {
        val runtimePermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return runtimePermissionGranted && NotificationManagerCompat.from(application).areNotificationsEnabled()
    }

    override suspend fun onNotificationPermissionGranted() {
        setBackgroundNotificationsEnabled(enabled = true)
    }

    override suspend fun setBackgroundNotificationsEnabled(enabled: Boolean) {
        keyValueStorage.putBoolean(BACKGROUND_NOTIFICATIONS_ENABLED_KEY, enabled)

        if (!enabled) {
            cancelPolling()
            clearObservedUnreadCount()
            return
        }

        if (!initializeObservedUnreadCount(force = true)) {
            logger.warn("Failed to seed PM unread count after enabling background notifications")
        }
        syncScheduling()
    }

    override suspend fun syncScheduling() {
        if (!authRepository.isLoggedIn()) {
            onLoggedOut()
            return
        }

        if (!isBackgroundNotificationsEnabled() || !areSystemNotificationsEnabled()) {
            cancelPolling()
            return
        }

        if (!initializeObservedUnreadCount(force = false)) {
            cancelPolling()
            return
        }

        enqueuePolling()
    }

    override suspend fun onLoggedOut() {
        cancelPolling()
        clearObservedUnreadCount()
    }

    override suspend fun updateObservedUnreadCount(unreadCount: Int) {
        if (!isBackgroundNotificationsEnabled() || !authRepository.isLoggedIn()) {
            return
        }
        keyValueStorage.putLong(LAST_OBSERVED_PM_UNREAD_COUNT_KEY, unreadCount.toLong())
    }

    override suspend fun showDebugPrivateMessagesNotification(): Boolean {
        if (!areSystemNotificationsEnabled()) {
            return false
        }

        showPrivateMessagesNotification(hasMultipleMessages = false)
        return true
    }

    suspend fun pollAndNotifyIfNeeded(): ListenableWorker.Result {
        if (!isBackgroundNotificationsEnabled()) {
            cancelPolling()
            return ListenableWorker.Result.success()
        }

        if (!authRepository.isLoggedIn()) {
            onLoggedOut()
            return ListenableWorker.Result.success()
        }

        val status = notificationsRepository.refreshStatus()
            .onFailure {
                logger.warn("Failed to refresh PM notifications status in background worker", it)
            }
            .getOrElse {
                return ListenableWorker.Result.retry()
            }

        val currentUnreadCount = status.privateMessagesUnreadCount
        val previousUnreadCount = keyValueStorage.getLong(LAST_OBSERVED_PM_UNREAD_COUNT_KEY)
            ?.takeIf { it >= 0 }
            ?.toInt()

        if (previousUnreadCount != null &&
            currentUnreadCount > previousUnreadCount &&
            areSystemNotificationsEnabled() &&
            !AppVisibilityTracker.isAppVisible
        ) {
            val hasMultipleMessages = currentUnreadCount - previousUnreadCount > 1
            showPrivateMessagesNotification(hasMultipleMessages = hasMultipleMessages)
        }

        keyValueStorage.putLong(
            LAST_OBSERVED_PM_UNREAD_COUNT_KEY,
            currentUnreadCount.toLong(),
        )

        return ListenableWorker.Result.success()
    }

    private suspend fun isBackgroundNotificationsEnabled(): Boolean =
        keyValueStorage.getBoolean(BACKGROUND_NOTIFICATIONS_ENABLED_KEY) ?: false

    private suspend fun initializeObservedUnreadCount(force: Boolean): Boolean {
        if (!force && keyValueStorage.getLong(LAST_OBSERVED_PM_UNREAD_COUNT_KEY)?.let { it >= 0 } == true) {
            return true
        }

        return notificationsRepository.refreshStatus()
            .onSuccess { status ->
                keyValueStorage.putLong(
                    LAST_OBSERVED_PM_UNREAD_COUNT_KEY,
                    status.privateMessagesUnreadCount.toLong(),
                )
            }
            .onFailure {
                logger.warn("Failed to seed PM unread count for background notifications", it)
            }
            .isSuccess
    }

    private fun enqueuePolling() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<PrivateMessagesPollingWorker>(
            repeatInterval = POLLING_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    private fun cancelPolling() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private suspend fun clearObservedUnreadCount() {
        keyValueStorage.putLong(LAST_OBSERVED_PM_UNREAD_COUNT_KEY, UNSET_UNREAD_COUNT)
    }

    @SuppressLint("MissingPermission")
    private fun showPrivateMessagesNotification(hasMultipleMessages: Boolean) {
        ensureNotificationChannel()

        val intent = Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = PRIVATE_MESSAGES_INBOX_DEEP_LINK.toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            application,
            PRIVATE_MESSAGES_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = if (hasMultipleMessages) {
            application.getString(R.string.pm_notification_title_plural)
        } else {
            application.getString(R.string.pm_notification_title)
        }

        val notification = NotificationCompat.Builder(application, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(application.getString(R.string.pm_notification_body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(application).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureNotificationChannel() {
        val notificationManager = application.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            application.getString(R.string.pm_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }

    private companion object {
        const val BACKGROUND_NOTIFICATIONS_ENABLED_KEY = "PM_BACKGROUND_NOTIFICATIONS_ENABLED"
        const val LAST_OBSERVED_PM_UNREAD_COUNT_KEY = "PM_LAST_OBSERVED_UNREAD_COUNT"
        const val UNIQUE_WORK_NAME = "private_messages_background_polling"
        const val NOTIFICATION_CHANNEL_ID = "private_messages_background_channel"
        const val NOTIFICATION_ID = 4001
        const val PRIVATE_MESSAGES_REQUEST_CODE = 4002
        const val PRIVATE_MESSAGES_INBOX_DEEP_LINK = "https://masslany.pl/app/private-messages"
        const val POLLING_INTERVAL_MINUTES = 15L
        const val UNSET_UNREAD_COUNT = -1L
    }
}
