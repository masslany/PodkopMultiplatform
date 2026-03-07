package pl.masslany.podkop

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.masslany.podkop.common.navigation.ExternalBrowser
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.common.platform.ImageDownloader
import pl.masslany.podkop.common.platform.ScreenshotExporter
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesBackgroundNotificationsController

val mainModule = module {
    viewModelOf(::MainActivityViewModel)
    single { AndroidActivityHolder() }
    single {
        AndroidPrivateMessagesBackgroundNotificationsController(
            application = androidApplication(),
            authRepository = get(),
            notificationsRepository = get(),
            keyValueStorage = get(),
            logger = get(),
        )
    }
    single<PrivateMessagesBackgroundNotificationsController> {
        get<AndroidPrivateMessagesBackgroundNotificationsController>()
    }
    single {
        BuildInfo(
            isDebugBuild = BuildConfig.DEBUG,
            appVersionName = BuildConfig.VERSION_NAME,
        )
    }

    single {
        ExternalBrowser(
            activityProvider = { get<AndroidActivityHolder>().activity },
            application = androidApplication(),
            logger = get(),
        )
    }

    single {
        ImageDownloader(
            application = androidApplication(),
        )
    }

    single {
        ScreenshotExporter(
            application = androidApplication(),
        )
    }
}
