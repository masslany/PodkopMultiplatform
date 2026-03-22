package pl.masslany.podkop.features.blacklists.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.blacklists.BlacklistsViewModel

val blacklistsModule = module {
    viewModel {
        BlacklistsViewModel(
            blacklistsRepository = get(),
            profileRepository = get(),
            tagsRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
