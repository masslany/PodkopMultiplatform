package pl.masslany.podkop

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.masslany.podkop.common.navigation.ExternalBrowser
import pl.masslany.podkop.common.platform.ImageDownloader

val mainModule = module {
    viewModelOf(::MainActivityViewModel)
    single { AndroidActivityHolder() }

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
}
