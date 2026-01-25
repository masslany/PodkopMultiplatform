package pl.masslany.podkop.common.navigation.di

import org.koin.dsl.module
import pl.masslany.podkop.common.navigation.AppConfigProvider
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.NavigationConfigProvider

val navigationModule = module {
    single<AppNavigator> {
        AppNavigator(
            scope = get(),
            configProvider = get(),
        )
    }

    factory<NavigationConfigProvider> {
        AppConfigProvider()
    }
}
