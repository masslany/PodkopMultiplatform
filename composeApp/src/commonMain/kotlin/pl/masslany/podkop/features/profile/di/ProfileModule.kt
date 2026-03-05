package pl.masslany.podkop.features.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.profile.ProfileViewModel

val profileModule = module {
    viewModel { params ->
        ProfileViewModel(
            username = params.get<String>(),
            profileRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
