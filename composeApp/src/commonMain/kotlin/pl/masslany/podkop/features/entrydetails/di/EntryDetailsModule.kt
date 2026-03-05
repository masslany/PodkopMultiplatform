package pl.masslany.podkop.features.entrydetails.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.entrydetails.EntryDetailsViewModel

val entryDetailsModule = module {
    viewModel { params ->
        EntryDetailsViewModel(
            screen = params.get(),
            entriesRepository = get(),
            authRepository = get(),
            profileRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            topBarActions = get(),
            snackbarManager = get(),
            appNavigator = get(),
        )
    }
}
