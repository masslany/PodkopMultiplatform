package pl.masslany.podkop.features.more.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.more.MoreViewModel

val moreModule = module {
    viewModel {
        MoreViewModel(
            authRepository = get(),
            authSessionEvents = get(),
            notificationsRepository = get(),
            profileRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
