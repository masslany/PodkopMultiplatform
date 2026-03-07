package pl.masslany.podkop

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PrivateMessagesPollingWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {
    private val notificationsController by inject<AndroidPrivateMessagesBackgroundNotificationsController>()

    override suspend fun doWork(): Result = notificationsController.pollAndNotifyIfNeeded()
}
