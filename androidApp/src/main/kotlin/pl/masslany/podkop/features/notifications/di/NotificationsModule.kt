package pl.masslany.podkop.features.notifications.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.notifications.NotificationsViewModel

val notificationsModule = module {
    viewModel {
        NotificationsViewModel(
            notificationsRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
