package pl.masslany.podkop.features.topbar.di

import org.koin.dsl.module
import pl.masslany.podkop.features.topbar.TopBarActions
import pl.masslany.podkop.features.topbar.TopBarActionsHandler

val topBarModule = module {
    factory<TopBarActions> {
        TopBarActionsHandler(
            appNavigator = get(),
        )
    }
}
