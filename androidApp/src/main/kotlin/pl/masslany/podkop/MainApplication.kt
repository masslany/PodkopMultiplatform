package pl.masslany.podkop

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesBackgroundNotificationsController
import timber.log.Timber

class MainApplication : Application() {

    private val mainScope = MainScope()

    private val startupManager by inject<StartupManager>()
    private val dispatcherProvider by inject<DispatcherProvider>()
    private val authRepository by inject<AuthRepository>()
    private val authSessionEvents by inject<AuthSessionEvents>()
    private val notificationsRepository by inject<NotificationsRepository>()
    private val privateMessagesBackgroundNotificationsController by inject<PrivateMessagesBackgroundNotificationsController>()

    override fun onCreate() {
        super.onCreate()

        plantLoggingTrees()
        registerActivityLifecycleCallbacks(AppVisibilityTracker)

        initKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(mainModule)
        }

        mainScope.launch(dispatcherProvider.io) {
            privateMessagesBackgroundNotificationsController.syncScheduling()
        }

        mainScope.launch(dispatcherProvider.io) {
            startupManager.init(
                key = BuildConfig.WYKOP_KEY,
                secret = BuildConfig.WYKOP_SECRET,
            )
        }

        mainScope.launch(dispatcherProvider.io) {
            authSessionEvents.events.collect {
                if (authRepository.isLoggedIn()) {
                    privateMessagesBackgroundNotificationsController.syncScheduling()
                } else {
                    privateMessagesBackgroundNotificationsController.onLoggedOut()
                }
            }
        }

        mainScope.launch(dispatcherProvider.io) {
            notificationsRepository.status
                .drop(1)
                .collect { status ->
                    privateMessagesBackgroundNotificationsController.updateObservedUnreadCount(
                        unreadCount = status.privateMessagesUnreadCount,
                    )
                }
        }
    }

    private fun plantLoggingTrees() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(CrashlyticsTree())
    }
}
