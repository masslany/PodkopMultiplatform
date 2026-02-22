package pl.masslany.podkop.common.deeplink.di

import org.koin.dsl.module
import pl.masslany.podkop.common.deeplink.AppDeepLinkHandler
import pl.masslany.podkop.common.deeplink.AppDeepLinkParser
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.deeplink.AuthSessionEventsImpl

val deepLinkModule = module {
    single { AppDeepLinkParser() }
    single<AuthSessionEvents> { AuthSessionEventsImpl() }
    single {
        AppDeepLinkHandler(
            scope = get(),
            parser = get(),
            startupManager = get(),
            appNavigator = get(),
            authRepository = get(),
            authSessionEvents = get(),
            logger = get(),
        )
    }
}
