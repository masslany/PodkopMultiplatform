package pl.masslany.podkop.features.settings.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.settings.SettingsViewModel

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            authRepository = get(),
            authSessionEvents = get(),
            appSettings = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            buildInfo = get(),
            topBarActions = get(),
        )
    }
}
